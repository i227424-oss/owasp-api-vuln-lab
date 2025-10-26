package edu.nu.owaspapivulnlab.web;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import edu.nu.owaspapivulnlab.model.AppUser;
import edu.nu.owaspapivulnlab.repo.AppUserRepository;
import edu.nu.owaspapivulnlab.web.dto.CreateUserRequest;
import edu.nu.owaspapivulnlab.web.dto.UserDto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * FIX-3: Enforce ownership checks
 * FIX-4: Return DTOs instead of entities
 * FIX-6: Prevent mass assignment
 */
@RestController
@RequestMapping("/api/users")
public class UserController {
    private final AppUserRepository users;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserController(AppUserRepository users, BCryptPasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * FIX-3: Users can only view their own profile
     * FIX-4: Return DTO instead of entity
     */
    @GetMapping("/{id}")
    public UserDto get(@PathVariable Long id, Authentication auth) {
        AppUser user = users.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // FIX-3: Check ownership - user can only view themselves
        AppUser currentUser = users.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("Current user not found"));
        
        if (!user.getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You can only view your own profile");
        }
        
        return UserDto.from(user);
    }

    /**
     * FIX-6: Use DTO to prevent mass assignment
     * FIX-4: Return DTO instead of entity
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@Valid @RequestBody CreateUserRequest request) {
        // FIX-6: Server controls role and isAdmin, not the client
        AppUser newUser = new AppUser();
        newUser.setUsername(request.username());
        newUser.setPassword(passwordEncoder.encode(request.password()));
        newUser.setEmail(request.email());
        newUser.setRole("USER"); // Always USER, never allow client to set
        newUser.setAdmin(false);  // Always false, never allow client to set
        
        newUser = users.save(newUser);
        return UserDto.from(newUser);
    }

    /**
     * REMOVED: Search endpoint removed to prevent user enumeration
     * Original vulnerability: API9 - allowed enumeration of users
     */
    
    /**
     * FIX-4: Return DTOs instead of entities
     * NOTE: This endpoint should ideally be admin-only
     */
    @GetMapping
    public List<UserDto> list() {
        return users.findAll().stream()
                .map(UserDto::from)
                .collect(Collectors.toList());
    }

    /**
     * FIX-5: Require ADMIN role for deletion (handled by SecurityConfig)
     * FIX-3: Additional check - users can only delete themselves unless admin
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication auth) {
        AppUser currentUser = users.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("Current user not found"));
        
        // FIX-3: Only allow deletion of own account, unless admin
        if (!currentUser.getId().equals(id) && !currentUser.isAdmin()) {
            throw new AccessDeniedException("You can only delete your own account");
        }
        
        users.deleteById(id);
        Map<String, String> response = new HashMap<>();
        response.put("status", "deleted");
        return ResponseEntity.ok(response);
    }
}
