package edu.nu.owaspapivulnlab.web;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import edu.nu.owaspapivulnlab.config.RateLimitConfig;
import edu.nu.owaspapivulnlab.model.Account;
import edu.nu.owaspapivulnlab.model.AppUser;
import edu.nu.owaspapivulnlab.repo.AccountRepository;
import edu.nu.owaspapivulnlab.repo.AppUserRepository;
import edu.nu.owaspapivulnlab.web.dto.AccountDto;
import edu.nu.owaspapivulnlab.web.dto.TransferRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * FIX-3: Enforce ownership checks on all account operations
 * FIX-4: Return DTOs instead of entities
 * FIX-5: Rate limiting on transfer endpoint
 * FIX-9: Input validation on transfer amounts
 */
@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountRepository accounts;
    private final AppUserRepository users;
    private final RateLimitConfig rateLimitConfig;

    public AccountController(AccountRepository accounts, AppUserRepository users, RateLimitConfig rateLimitConfig) {
        this.accounts = accounts;
        this.users = users;
        this.rateLimitConfig = rateLimitConfig;
    }

    /**
     * FIX-3: Check ownership before returning balance
     * FIX-4: Return DTO instead of raw balance
     */
    @GetMapping("/{id}/balance")
    public AccountDto balance(@PathVariable Long id, Authentication auth) {
        Account account = accounts.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        
        // FIX-3: Verify ownership
        AppUser currentUser = users.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!account.getOwnerUserId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not have permission to access this account");
        }
        
        return AccountDto.from(account);
    }

    /**
     * FIX-3: Check ownership before allowing transfer
     * FIX-5: Apply rate limiting
     * FIX-9: Validate transfer amount (no negative or excessive amounts)
     */
    @PostMapping("/{id}/transfer")
    public ResponseEntity<?> transfer(@PathVariable Long id, 
                                      @Valid @RequestBody TransferRequest request,
                                      Authentication auth) {
        // FIX-5: Rate limiting check
        String rateLimitKey = auth.getName() + ":transfer";
        if (!rateLimitConfig.resolveBucket(rateLimitKey).tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("error", "Rate limit exceeded. Please try again later."));
        }

        Account account = accounts.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        
        // FIX-3: Verify ownership
        AppUser currentUser = users.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!account.getOwnerUserId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not have permission to access this account");
        }

        // FIX-9: Additional validation for reasonable transfer amounts
        Double amount = request.amount();
        if (amount > 1000000) {
            throw new IllegalArgumentException("Transfer amount exceeds maximum limit");
        }
        
        if (amount > account.getBalance()) {
            throw new IllegalArgumentException("Insufficient funds");
        }

        account.setBalance(account.getBalance() - amount);
        accounts.save(account);
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ok");
        response.put("remaining", account.getBalance());
        return ResponseEntity.ok(response);
    }

    /**
     * FIX-4: Return DTOs instead of entities
     */
    @GetMapping("/mine")
    public List<AccountDto> mine(Authentication auth) {
        AppUser me = users.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return accounts.findAllByOwnerUserId(me.getId()).stream()
                .map(AccountDto::from)
                .collect(Collectors.toList());
    }
}
