package edu.nu.owaspapivulnlab.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * FIX-8: Secure error handler - reduces information disclosure
 * - No stack traces exposed to client
 * - Generic error messages
 * - Detailed errors logged server-side only
 */
@ControllerAdvice
public class GlobalErrorHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalErrorHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> all(Exception e) {
        // FIX-8: Log detailed error server-side
        logger.error("Unexpected error occurred", e);
        
        // FIX-8: Return generic message to client
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("error", "An internal error occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorMap);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<?> db(DataAccessException e) {
        // FIX-8: Log detailed error server-side
        logger.error("Database error occurred", e);
        
        // FIX-8: Return generic message to client
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("error", "A database error occurred");
        return ResponseEntity.status(500).body(errorMap);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> accessDenied(AccessDeniedException e) {
        logger.warn("Access denied: {}", e.getMessage());
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("error", "Access denied");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorMap);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> illegalArgument(IllegalArgumentException e) {
        logger.warn("Invalid request: {}", e.getMessage());
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("error", e.getMessage()); // Safe to expose validation errors
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMap);
    }
}
