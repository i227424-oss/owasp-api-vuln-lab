package edu.nu.owaspapivulnlab.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import edu.nu.owaspapivulnlab.model.AppUser;
import edu.nu.owaspapivulnlab.repo.AppUserRepository;
import edu.nu.owaspapivulnlab.service.JwtService;
import edu.nu.owaspapivulnlab.web.dto.SignupRequest;
import edu.nu.owaspapivulnlab.web.dto.UserDto;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AppUserRepository users;
    private final JwtService jwt;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthController(AppUserRepository users, JwtService jwt, BCryptPasswordEncoder passwordEncoder) {
        this.users = users;
        this.jwt = jwt;
        this.passwordEncoder = passwordEncoder;
    }

    // ==================== SIGNUP ENDPOINT ====================
    // FIX-1: Secure signup that hashes password before saving
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto signup(@Valid @RequestBody SignupRequest req) {
        if (users.findByUsername(req.username()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        AppUser newUser = new AppUser();
        newUser.setUsername(req.username());
        newUser.setPassword(passwordEncoder.encode(req.password())); // BCrypt hash
        newUser.setRole("USER");
        newUser.setAdmin(false);

        newUser = users.save(newUser);
        return UserDto.from(newUser);
    }

    // ==================== LOGIN ENDPOINT ====================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginReq req) {
        AppUser user = users.findByUsername(req.username()).orElse(null);

        // FIX-1: Use BCrypt to compare passwords instead of plaintext
        if (user != null && passwordEncoder.matches(req.password(), user.getPassword())) {
            Map<String, Object> claims = new HashMap<>();
            claims.put("role", user.getRole());
            claims.put("isAdmin", user.isAdmin());
            claims.put("uid", user.getId()); // add userId claim for ownership enforcement

            String token = jwt.issue(user.getUsername(), claims);
            return ResponseEntity.ok(new TokenRes(token));
        }

        Map<String, String> error = new HashMap<>();
        error.put("error", "invalid credentials");
        return ResponseEntity.status(401).body(error);
    }

    // ==================== REQUEST/RESPONSE CLASSES ====================
    public static class LoginReq {
        @NotBlank
        private String username;
        @NotBlank
        private String password;

        public LoginReq() {}
        public LoginReq(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String username() { return username; }
        public String password() { return password; }
        public void setUsername(String username) { this.username = username; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class TokenRes {
        private String token;

        public TokenRes() {}
        public TokenRes(String token) { this.token = token; }
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
    }
}
