package edu.nu.owaspapivulnlab.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {
    private SecurityUtils() {}

    public static String currentUsernameOrThrow() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) throw new RuntimeException("Unauthenticated");
        return auth.getPrincipal().toString(); // we set principal = username in JwtFilter
    }
}
