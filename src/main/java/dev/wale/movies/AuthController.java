package dev.wale.movies;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;


@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AuthController {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request){
        if (userRepository.existsByEmail(request.email())){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already in use");
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role("USER")
                .build();
        
        userRepository.save(user);

        String token = jwtService.generateToken(user);
        return ResponseEntity.ok(new AuthResponse(token));
    }
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@ResponseBody RegisterRequest request ){
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        User user = userRepository.findByEmail(request.email()).orElseThrow();

        String token = jwtService.generateToken(user);
        return ResponseEntity.ok(new AuthResponse(token));
    }

}
