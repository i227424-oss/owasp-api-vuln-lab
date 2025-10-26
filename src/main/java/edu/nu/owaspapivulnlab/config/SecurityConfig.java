package edu.nu.owaspapivulnlab.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * FIX-2: Harden SecurityFilterChain
 * - Only /api/auth/** and /actuator/health are public
 * - Admin endpoints require ADMIN role
 * - All other endpoints require authentication
 * - Proper JWT validation with error handling
 * FIX-7: Validate JWT issuer and audience
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.issuer}")
    private String issuer;

    @Value("${app.jwt.audience}")
    private String audience;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // APIs are stateless; disable CSRF for REST, enforce token auth
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/api/auth/**", "/actuator/health").permitAll()
                // Restrict admin endpoints
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                // Everything else requires authentication
                .anyRequest().authenticated()
            )
            // Add our improved JWT filter
            .addFilterBefore(new JwtFilter(secret, issuer, audience),
                    org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);

        // Disable frame options only if you need H2 console during local dev
        http.headers(headers -> headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }

    /**
     * FIX-2: Improved JWT Filter
     * - Verifies token signature properly
     * - Handles invalid tokens with 401 response
     * - Adds user roles from claims
     * FIX-7: Validates issuer and audience
     */
    static class JwtFilter extends OncePerRequestFilter {
        private final String secret;
        private final String issuer;
        private final String audience;
        
        JwtFilter(String secret, String issuer, String audience) { 
            this.secret = secret;
            this.issuer = issuer;
            this.audience = audience;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                throws ServletException, IOException {
            String auth = request.getHeader("Authorization");
            if (auth != null && auth.startsWith("Bearer ")) {
                String token = auth.substring(7);
                try {
                    Claims claims = Jwts.parserBuilder()
                            .setSigningKey(secret.getBytes())
                            .requireIssuer(issuer)
                            .requireAudience(audience)
                            .build()
                            .parseClaimsJws(token)
                            .getBody();

                    String username = claims.getSubject();
                    String role = (String) claims.get("role");

                    if (username != null) {
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        username, null,
                                        role != null
                                                ? Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                                                : Collections.emptyList());
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                } catch (JwtException e) {
                    // FIX: return 401 instead of ignoring token errors
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("Invalid or expired token");
                    return;
                }
            }
            chain.doFilter(request, response);
        }
    }
}
