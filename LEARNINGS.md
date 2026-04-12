# Authentication & Deployment Learnings

Notes from setting up JWT authentication with Auth0 and deploying to Railway.

---

## 1. What a JWT actually is

A JWT (JSON Web Token) is a string with three parts separated by dots:

```
eyJhbGciOi...  .  eyJzdWIiOi...  .  <signature>
   header         payload          signature
```

- **Header** — says how the token is signed (algorithm).
- **Payload** — the claims: who the token is for, when it expires, who issued it. Plain JSON, base64-encoded. Anyone can read this.
- **Signature** — a cryptographic signature proving the token was issued by whoever owns the signing key.

The critical insight: **JWTs are not encrypted, they are signed.** Anyone can read the contents. What signing guarantees is that nobody can *modify* the contents without invalidating the signature. That's how trust works — the server trusts the token because it can verify the signature with a public key.

### Standard claims

- `iss` (issuer) — who issued the token (e.g., your Auth0 tenant URL)
- `sub` (subject) — who the token is about (user ID, or in M2M, the client ID)
- `aud` (audience) — who the token is meant for (your API identifier)
- `exp` (expiration) — when the token stops being valid
- `iat` (issued at) — when the token was created

Spring validates all of these automatically.

---

## 2. The two models of JWT auth

When you see "JWT authentication" in Spring, it can mean one of two very different things. The distinction is **who issues the token**.

### Option A: External Identity Provider (Resource Server)

Someone else (Auth0, Keycloak, Okta, Cognito) handles user signup, login, password resets, and issues JWTs. Your app only **validates** those tokens. You never touch passwords. This is what `spring-boot-starter-oauth2-resource-server` is designed for.

**When to use it:** you want real users fast, want features like Google login, don't want to manage auth code.

### Option B: Self-Issued JWTs

Your app is **both** the issuer and the validator. You build `/register` and `/login` endpoints, hash passwords with BCrypt, sign JWTs with a secret you own. No external IdP.

**When to use it:** full control over user schema, learning how auth works under the hood, avoiding external dependencies.

### Why they're not interchangeable

Both models share a key idea — your app validates JWTs on every request and puts the authenticated user into Spring's `SecurityContext`. But the token source is completely different, and so is the Spring Security configuration. Switching from A to B later is a contained refactor because your business logic (services, repositories) doesn't depend on auth — but the security layer itself has to be rewritten.

**I went with Option A (Auth0).** Plan is to refactor to Option B later for learning.

---

## 3. Auth0's two-sided concept

This tripped me up. Auth0 has two separate dashboard areas that **look similar but mean different things**:

| Sidebar | What it represents | What you get |
|---|---|---|
| **Applications → APIs** | The thing being protected (my Spring Boot app) | API Identifier (`audience`) |
| **Applications → Applications** | Things that request tokens (Postman, a frontend, a test client) | `client_id` and `client_secret` |

To get a working token flow, I needed **both**:
- An **API** (registered once, gives me the `audience` value I set myself, e.g. `https://movies-api`).
- A **Machine-to-Machine Application** authorized to request tokens for that API.

### Two different URLs

Another thing I confused at first:

| Thing | Example | Purpose |
|---|---|---|
| **Tenant domain / issuer** | `https://dev-l5bkn4y4mnbgk18u.eu.auth0.com/` | Where Auth0 lives. Goes in `JWT_ISSUER_URI`. **Trailing slash required.** |
| **API identifier / audience** | `https://movies-api` | A label I chose. Goes in `JWT_AUDIENCE`. Does NOT have to resolve to a real URL. |
| **Token endpoint** | `https://dev-l5bkn4y4mnbgk18u.eu.auth0.com/oauth/token` | URL Postman POSTs to when requesting a token. |

---

## 4. How a request flows through Spring Security

Understanding this flow made everything click. When a `POST /api/v1/reviews` hits my app with `Authorization: Bearer eyJ...`:

1. **Filter chain runs before the controller.** Spring Security has a stack of filters. For JWT auth, the `BearerTokenAuthenticationFilter` (added by `.oauth2ResourceServer(oauth -> oauth.jwt())`) runs automatically.

2. **Filter extracts the token** from the `Authorization` header.

3. **Filter fetches Auth0's public keys** from `<issuer>/.well-known/jwks.json` (cached after first fetch).

4. **Filter verifies the signature** using those public keys. This proves Auth0 actually signed the token.

5. **Filter validates claims**:
   - `iss` matches `JWT_ISSUER_URI`
   - `aud` matches `JWT_AUDIENCE`
   - `exp` hasn't passed

6. **Filter builds a `Jwt` object** and puts it into Spring's `SecurityContext`.

7. **Only now does the controller run.** The controller uses `@AuthenticationPrincipal Jwt jwt` to pull the validated token out of the context. `jwt.getSubject()` returns the `sub` claim (the user ID, or for M2M, the client ID).

8. **Controller calls the service layer.** Service is completely auth-unaware. It just takes a `String userId` and stores it on the review. This separation of concerns is what makes the auth layer swappable.

### The mental model

- **Auth0** = identity provider. Signs tokens. Publishes public keys.
- **Spring Security Resource Server** = JWT validator. Fetches the public keys, checks every request, rejects or allows.
- **My controllers** = never deal with tokens directly. They trust `@AuthenticationPrincipal`.
- **My services** = completely unaware of auth. They take plain strings.

---

## 5. Issues I ran into and how I fixed them

### Issue 1: Dockerfile typo (`mvw` instead of `mvnw`)

**Symptom:** Docker build would fail with "file not found" during the `chmod` step.

**Cause:** Classic copy-paste typo. Would have failed on Railway with no useful error.

**Fix:** Change `RUN chmod +x mvw` to `RUN chmod +x mvnw`.

**Lesson:** Always test `docker build` locally before deploying. Typos in Dockerfiles are invisible until you actually run the build.

---

### Issue 2: `.env` file quotes breaking MongoDB connection

**Symptom:** When running inside Docker, the app crashed at startup with:
```
Database name must not contain slashes, dots, spaces, quotes, or dollar signs
```

Meanwhile, the app worked fine when launched from IntelliJ/VS Code locally.

**Cause:** My `.env` file had quoted values:
```
MONGO_DATABASE="movie-api-db"
```

`spring-dotenv` (the library I used for local dev) **strips** surrounding quotes when parsing. Docker's `--env-file` parser **does not** — it treats the quotes as literal characters. So inside Docker, `MONGO_DATABASE` was literally `"movie-api-db"` (with quotes), which Spring/MongoDB rejected because database names can't contain quote characters.

Log evidence showed `userName='"WaleDatabase"'` — the extra quotes were part of the value.

**Fix:** Remove quotes from `.env`:
```
MONGO_DATABASE=movie-api-db
```

**Lesson:** Different `.env` parsers have different quote-handling rules. The safest format is `KEY=value` with no quotes, which works in both `spring-dotenv`, Docker, Railway's dashboard, and most other tools. Also: **always test in Docker locally before deploying** — this bug would have been much harder to debug on Railway.

---

### Issue 3: Railway 502 "Application failed to respond"

**Symptom:** Railway build and deployment succeeded. Logs showed `Started MoviesApplication in X seconds`. But hitting the public URL returned:
```
{ "status": "error", "code": 502, "message": "Application failed to respond" }
```

**Cause:** Port mismatch between what Spring was listening on and what Railway's proxy was trying to route to.

- `application.properties` had `server.port=${PORT:8081}` (use `PORT` env var, fall back to 8081).
- Railway injected `PORT=8080`.
- Spring correctly bound to **8080**.
- But I'd set Railway's **Target Port** to **8081**, so the proxy was routing traffic to a port where nothing was listening.

**Fix:** Change Target Port in Railway → Settings → Networking to `8080` (match the port shown in `Tomcat started on port XXXX` in the logs).

**Lesson:** `${PORT:FALLBACK}` means "use PORT if set, else FALLBACK." The fallback is the **local dev value** — the actual runtime port on a platform is whatever the platform injects. Always check the startup logs for `Tomcat started on port XXXX` to confirm which port the app is actually bound to, and make sure your proxy/ingress routes to the same port.

---

### Issue 4: 401 Unauthorized on the root URL

**Symptom:** Opening `https://<railway-domain>/` in a browser returned:
```
HTTP ERROR 401
```

**Cause:** This wasn't a bug — it was correct behavior.

My `SecurityConfig` has:
```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers(HttpMethod.GET, "/api/v1/movies/**").permitAll()
    .requestMatchers(HttpMethod.GET, "/api/v1/reviews/**").permitAll()
    .anyRequest().authenticated()
)
```

Only specific paths are public. The root URL (`/`) falls into `.anyRequest().authenticated()`, which requires a valid JWT. Spring Security rejects unauthenticated requests to unlisted routes with 401.

**Fix:** None needed. Hit the actual API routes instead of `/`:
```
GET https://<railway-domain>/api/v1/movies
```

**Lesson:** A REST API has no root route. 401 on `/` is a sign that security is working, not that something is broken. The failure mode "I visited the URL in a browser and got an error" is almost always the wrong test for a REST API — use Postman against real endpoints.

---

### Issue 5: Quirky MongoDB `id` in JSON responses

**Symptom:** The `id` field in review responses came back as:
```json
"id": {
  "timestamp": 1775923943,
  "date": "2026-04-11T16:12:23.000+00:00"
}
```

instead of a plain hex string like `"670f1234abcd5678ef901234"`.

**Cause:** MongoDB's `ObjectId` is a 12-byte binary value, not a string. Jackson doesn't know how to serialize it to a hex string by default, so it falls back to serializing the object's public properties.

**Fix (not yet applied):** I already have custom serializers in `ObjectIdSerializer.java` and `ObjectIdDeserializer.java`, but the annotations on `Review.id` are commented out. Uncommenting them will return the id as a clean hex string.

**Lesson:** Jackson + MongoDB `ObjectId` needs custom serializers. This is a common friction point in Spring Data MongoDB projects.

---

## 6. Docker concepts I actually understand now

### Docker Desktop vs. Docker Hub

- **Docker Desktop** = the local app that runs containers. The engine. Needed for `docker build` and `docker run` to work on Mac.
- **Docker Hub** = a website (hub.docker.com) where people publish images. The "GitHub of Docker images." Used when Dockerfiles say `FROM eclipse-temurin:21-jdk` — Docker pulls that image from Docker Hub.
- They are completely separate. You can use one without the other.

### "Running in Docker" is still running on your laptop

A Docker container runs on your Mac, in an isolated Linux environment managed by Docker Desktop. The container uses your Mac's CPU, RAM, and network. It's not remote. The isolation is what gives you reproducibility — the container has only what the Dockerfile put in it, no dependency on your installed JDK, your Maven cache, or your shell environment.

### The real value: dev == prod

The same image that runs on my laptop runs on Railway. Same OS, same JDK, same everything. If it works locally in Docker, it will almost certainly work in production. This is how I caught the `.env` quote bug — the local Docker test exposed it before it hit Railway.

### Multi-stage build pattern

My production Dockerfile has two stages:

1. **Build stage** uses the JDK image to compile the jar with Maven.
2. **Runtime stage** copies just the jar into a smaller JRE image (no JDK, no Maven, no source code).

This keeps the final image small (~220 MB instead of ~450 MB) and reduces attack surface.

### Non-root user

By default, containers run as root. My Dockerfile creates a `spring` user and runs the app as that user. Small hardening step.

---

## 7. Deployment concepts

### Railway's GitHub integration vs. GitHub Actions

Railway can deploy directly from a GitHub repo — it reads the Dockerfile and builds on their servers. No CI pipeline needed. For a solo learning project without meaningful tests, this is enough.

**When to add GitHub Actions later:**
- When I have real tests to run.
- When I need to gate deploys on passing tests.
- When I'm collaborating and can't trust everyone to run tests locally.
- When I want automated dependency updates (Dependabot).
- When I want pre-deploy steps like schema migrations.

Not before. Adding CI ceremony without a reason is just more YAML to maintain.

### Environment variables as configuration

Twelve-Factor App principle: config lives in env vars, not code. My `application.properties` references:
- `${MONGO_DATABASE}`
- `${MONGO_USER}`
- `${MONGO_PASSWORD}`
- `${MONGO_CLUSTER}`
- `${JWT_ISSUER_URI}`
- `${JWT_AUDIENCE}`
- `${PORT:8081}` (with local fallback)

The same code runs locally (reading from `.env`) and on Railway (reading from Railway's Variables tab). No code changes between environments.

### MongoDB Atlas IP allowlist

Railway's outbound IPs aren't fixed. The only workable option for Atlas is `0.0.0.0/0` (allow from anywhere). This is standard for platforms like Railway/Heroku/Render. Security comes from strong DB credentials, not IP restrictions.

---

## 8. Key takeaways for the future

1. **Test in Docker locally before deploying anywhere.** The `.env` quote bug would have been brutal to debug on Railway. Running `docker build && docker run` on my laptop caught it in seconds.

2. **Always check startup logs for the actual bound port.** `Tomcat started on port XXXX` is the source of truth — not what I hoped the app would bind to.

3. **`${VAR:fallback}` is a local-dev fallback, not a production value.** The actual value on a platform is whatever the platform injects. Don't assume the fallback is what runs in prod.

4. **`.env` quote handling varies by parser.** The safest format is unquoted: `KEY=value`. Works in Docker, Railway, spring-dotenv, and most other tools.

5. **Separation of concerns is what makes auth swappable.** My service layer doesn't know anything about JWTs, Auth0, or Spring Security — it just takes a `String userId`. That means I can refactor from Option A (Auth0) to Option B (self-issued) without touching the business logic. Good design pays off during refactors.

6. **A 401 is often a sign things are working, not broken.** Unauthenticated access to a protected endpoint returning 401 is correct behavior. Test protected endpoints with a real token, not by "visiting the URL."

7. **A 502 is infrastructure, not application.** When Railway returns 502, the app is either not running or listening on the wrong port. Check the logs first; don't assume it's a code bug.

8. **Multi-stage Docker builds are standard for Java.** Build with JDK, run with JRE, keep the final image small.

9. **Deploy first, optimize later.** A working deployment via Railway's GitHub integration teaches you more than a complex CI pipeline that you don't understand. Add CI when you have a concrete reason, not because it sounds professional.

10. **Auth has two sides in Auth0's model.** An API (what's protected) and an Application (what requests tokens). Getting this right is the biggest conceptual hurdle — once you see it, the rest of the config makes sense.

---

## 9. What's next

Things I haven't done yet but should consider:

- [ ] Uncomment ObjectId serializers in `Review.java` for cleaner JSON
- [ ] Rotate the MongoDB password (exposed in conversation history)
- [ ] Add Spring Boot Actuator for health checks (`/actuator/health`)
- [ ] Add real integration tests beyond `contextLoads()`
- [ ] Add GitHub Actions to run those tests on every push
- [ ] Refactor to Option B (self-issued JWTs) as a learning exercise
- [ ] Add rate limiting on the auth endpoints
- [ ] Add Flyway or similar for database migrations (when the schema evolves)
- [ ] Set up error tracking (Sentry) or structured logging
