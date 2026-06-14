# Movie Review App — Backend

A Spring Boot REST API for browsing movies, posting reviews, and managing user accounts. Built with Spring Boot 3.4, Java 21, MongoDB Atlas, and JWT-based authentication.

## Live Deployment

- **Base URL:** https://movie-review-backend-production-d38f.up.railway.app
- **Hosted on:** [Railway](https://railway.app)

## API Documentation (Swagger / OpenAPI)

The API is documented with Swagger UI via [springdoc-openapi](https://springdoc.org/).

- **Live Swagger UI:** https://movie-review-backend-production-d38f.up.railway.app/swagger-ui/index.html
- **Local Swagger UI:** http://localhost:8080/swagger-ui/index.html
- **Raw OpenAPI JSON:** https://movie-review-backend-production-d38f.up.railway.app/v3/api-docs

### Using the docs to test protected endpoints

1. Open the Swagger UI link above.
2. Call `POST /api/v1/auth/register` or `POST /api/v1/auth/login` and copy the returned `token`.
3. Click the green **Authorize** button (top-right) and paste the token.
4. You can now hit any JWT-protected endpoint directly from the UI.

## Features

- **Movies:** browse, fetch by ID, create, update, delete.
- **Reviews:** create, fetch reviews for a movie.
- **Authentication:** register and login with JWT (stateless, Bearer token).
- **Authorization:** per-route role/ownership rules via Spring Security.
- **Validation:** request body validation with `spring-boot-starter-validation`.
- **API Docs:** auto-generated Swagger UI.

## Tech Stack

- **Language / Runtime:** Java 21
- **Framework:** Spring Boot 3.4.1
- **Database:** MongoDB Atlas
- **Security:** Spring Security + JWT (jjwt 0.12.6)
- **Docs:** springdoc-openapi 2.6.0
- **Build:** Maven
- **Deployment:** Railway (Docker)

## API Endpoints

> All endpoints are prefixed with the base URL above. Send/receive JSON.

### Auth — `/api/v1/auth`
| Method | Path | Description | Auth |
|--------|------|-------------|------|
| POST | `/register` | Create a new user, returns a JWT | Public |
| POST | `/login` | Log in, returns a JWT | Public |

### Movies — `/api/v1/movies`
| Method | Path | Description | Auth |
|--------|------|-------------|------|
| GET | `/` | List all movies | Public |
| GET | `/{id}` | Get a movie by ID | Public |
| GET | `/mine` | List movies created by the logged-in user | Required |
| POST | `/` | Create a movie | Required |
| PUT | `/{id}` | Update a movie | Required |
| DELETE | `/{id}` | Delete a movie | Required |

### Reviews — `/api/v1/reviews`
| Method | Path | Description | Auth |
|--------|------|-------------|------|
| GET | `/{movieId}` | List reviews for a movie | Public |
| POST | `/` | Submit a review | Required |

For full request/response schemas and example payloads, use the **Swagger UI** link above.

## Local Development

### Prerequisites
- Java 21 (JDK)
- Maven (or use the included `./mvnw` wrapper)
- A MongoDB Atlas cluster (free tier is fine)

### Setup

1. **Clone the repo:**
   ```bash
   git clone <repo-url>
   cd movies
   ```

2. **Configure environment variables.** Copy `.env.example` to `.env` and fill in:
   ```env
   MONGODB_URI=mongodb+srv://<user>:<password>@<cluster>/moviereview
   JWT_SECRET=<a-long-random-string>
   JWT_EXPIRATION=86400000
   ```

3. **Run the app:**
   ```bash
   ./mvnw spring-boot:run
   ```

4. **Verify:**
   - API root: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui/index.html

### Run with Docker

```bash
docker build -t movies-backend .
docker run -p 8080:8080 --env-file .env movies-backend
```

## Project Structure

```
src/main/java/dev/wale/movies/
├── MoviesApplication.java       # Entry point
├── SecurityConfig.java          # Spring Security + JWT filter chain
├── OpenApiConfig.java           # Swagger / OpenAPI metadata
├── JwtService.java              # Token generation & validation
├── JwtAuthFilter.java           # Per-request JWT auth filter
├── AuthController.java          # /api/v1/auth
├── MovieController.java         # /api/v1/movies
├── ReviewController.java        # /api/v1/reviews
├── Movie.java / Review.java / User.java   # Mongo documents
└── ...Request / ...Response / ...Repository
```

## Authentication Flow

1. Client calls `POST /api/v1/auth/register` or `/login` with email + password.
2. Server returns a JWT in the response body.
3. Client sends the JWT in subsequent requests as:
   ```
   Authorization: Bearer <token>
   ```
4. `JwtAuthFilter` validates the token on each request and populates the security context.

## Error Responses

The API returns JSON for auth failures:

```json
{ "error": "Unauthorized", "message": "Authentication required. Please provide a valid token." }
```

```json
{ "error": "Forbidden", "message": "You do not have permission to access this resource." }
```

## Roadmap

- Watchlist feature
- Pagination & filtering on movie list
- Refresh tokens
- Integration tests for controller layer
- CI/CD via GitHub Actions

## License

MIT

## Contact

Built by Wale Fagbodun — [LinkedIn](https://www.linkedin.com/in/adewale-wale-fagbodun)
