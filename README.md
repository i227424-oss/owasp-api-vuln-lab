# OWASP API Vulnerable Lab (Spring Boot + JWT)

> ~~This project intentionally contains vulnerabilities mapped to **OWASP API Security Top 10 (2023)** 
> so students can identify and fix them.~~

## ✅ **ALL VULNERABILITIES HAVE BEEN FIXED!**

This project now implements all 10 security best practices from the OWASP API Security Top 10.

See **[SECURITY_FIXES_SUMMARY.md](SECURITY_FIXES_SUMMARY.md)** for detailed implementation information.

## Quick Start

```bash
# Java 17 + Maven required
mvn clean install
mvn spring-boot:run
# H2 Console: http://localhost:8080/h2-console (JDBC URL: jdbc:h2:mem:apilab)
```

For detailed API usage, see **[QUICK_START.md](QUICK_START.md)**

## Seed Users

- `alice / alice123` (USER) - Owns account PK00-ALICE
- `bob / bob123` (ADMIN) - Owns account PK00-BOB

**Note:** Passwords are now BCrypt-hashed in the database!

Login to get a JWT:
```bash
curl -s -X POST http://localhost:8080/api/auth/login -H 'Content-Type: application/json' -d '{"username":"alice","password":"alice123"}'
# => {"token":"<JWT>"}
```

Use the token:
```bash
export T="<JWT>"
curl -H "Authorization: Bearer $T" http://localhost:8080/api/accounts/mine
```

## ✅ Security Features Implemented

### 1. **BCrypt Password Hashing**
- All passwords hashed with BCrypt
- Secure password comparison
- No plaintext passwords in database

### 2. **Hardened Security Filter Chain**
- Only `/api/auth/**` is public
- All other endpoints require authentication
- Admin endpoints require ADMIN role
- JWT validation with issuer/audience checks

### 3. **Ownership Enforcement**
- Users can only access their own resources
- Account balance/transfer restricted to owner
- User profile access restricted to self

### 4. **DTOs for Data Exposure Control**
- `UserDto` - Hides password, role, isAdmin
- `AccountDto` - Safe account representation
- `CreateUserRequest` - Prevents privilege escalation
- `TransferRequest` - Validated transfer data

### 5. **Rate Limiting**
- Bucket4j implementation
- 5 transfers per minute per user
- 429 Too Many Requests on exceeded limit

### 6. **Mass Assignment Prevention**
- CreateUserRequest DTO without role/isAdmin fields
- Server controls all privilege assignments
- Always creates users with USER role

### 7. **Hardened JWT**
- Strong secret from environment variable
- 1-hour token expiry (reduced from 30 days)
- Issuer and audience validation
- Strict signature verification

### 8. **Reduced Error Details**
- Generic error messages to clients
- Detailed logging server-side only
- No stack traces exposed
- Proper HTTP status codes

### 9. **Input Validation**
- Positive amount validation
- Maximum transfer limit (1,000,000)
- Insufficient funds check
- Bean validation with `@Valid`

### 10. **Integration Tests**
- Authentication requirement tests
- Authorization/ownership tests
- Mass assignment prevention tests
- JWT validation tests

## Security Vulnerabilities Fixed

### ✅ API1: Broken Object Level Authorization (BOLA/IDOR)
**Fixed:** Ownership checks on all account and user operations

### ✅ API2: Broken Authentication
**Fixed:** BCrypt password hashing, hardened JWT with issuer/audience

### ✅ API3: Excessive Data Exposure
**Fixed:** DTOs hide sensitive fields (password, role, isAdmin)

### ✅ API4: Unrestricted Resource Consumption
**Fixed:** Rate limiting with Bucket4j (5 transfers/minute)

### ✅ API5: Broken Function Level Authorization
**Fixed:** Proper role enforcement via SecurityFilterChain

### ✅ API6: Mass Assignment
**Fixed:** Request DTOs without privilege fields, server controls role

### ✅ API7: Security Misconfiguration
**Fixed:** Environment-based secrets, no error details exposed

### ✅ API8: Weak Authentication / JWT issues
**Fixed:** Strong JWT secret, short TTL, issuer/audience validation

### ✅ API9: Improper Inventory / Injection
**Fixed:** Removed search endpoint, input validation, parameterized queries

### ✅ API10: Insufficient Logging & Monitoring
**Fixed:** Server-side logging without client exposure

## Project Structure

```
src/main/java/edu/nu/owaspapivulnlab/
├── config/
│   ├── DataSeeder.java          ✅ BCrypt password hashing
│   ├── SecurityBeans.java
│   ├── SecurityConfig.java      ✅ Hardened filter chain + JWT validation
│   └── RateLimitConfig.java     ✅ NEW - Rate limiting
├── model/
│   ├── Account.java
│   └── AppUser.java
├── repo/
│   ├── AccountRepository.java
│   └── AppUserRepository.java
├── security/
│   └── SecurityUtils.java
├── service/
│   └── JwtService.java          ✅ Issuer/audience + short TTL
└── web/
    ├── AccountController.java   ✅ Ownership + DTOs + rate limiting
    ├── AdminController.java
    ├── AuthController.java      ✅ BCrypt signup/login
    ├── GlobalErrorHandler.java  ✅ Reduced error exposure
    ├── UserController.java      ✅ Ownership + DTOs + mass assignment prevention
    └── dto/
        ├── AccountDto.java      ✅ NEW
        ├── CreateUserRequest.java ✅ NEW
        ├── SignupRequest.java
        ├── TransferRequest.java ✅ NEW
        └── UserDto.java
```

## Environment Variables

### Production JWT Secret:
```bash
# Windows PowerShell
$env:APP_JWT_SECRET="your-very-long-and-secure-secret-key-here-at-least-256-bits"

# Linux/Mac
export APP_JWT_SECRET="your-very-long-and-secure-secret-key-here-at-least-256-bits"
```

## Run Tests

```bash
mvn test
```

All integration tests in `AdditionalSecurityExpectationsTests.java` should now pass!

## Documentation

- **[SECURITY_FIXES_SUMMARY.md](SECURITY_FIXES_SUMMARY.md)** - Detailed security implementation guide
- **[QUICK_START.md](QUICK_START.md)** - API usage and testing guide
- **[README.md](README.md)** - This file

## Notes

All 10 security requirements have been successfully implemented:
1. ✅ BCrypt passwords
2. ✅ Hardened SecurityFilterChain
3. ✅ Ownership enforcement
4. ✅ DTOs for data exposure
5. ✅ Rate limiting
6. ✅ Mass assignment prevention
7. ✅ JWT hardening
8. ✅ Error detail reduction
9. ✅ Input validation
10. ✅ Integration tests

