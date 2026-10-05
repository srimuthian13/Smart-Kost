# Rate Limiting and Validation Improvements

The `auth_service` has been successfully updated to address API security and data validation.

## Changes Made

1. **API Validation Fixed**:
   - Added the missing `spring-boot-starter-validation` dependency to `pom.xml`.
   - Now, annotations like `@Valid`, `@NotBlank`, and `@Email` in `RegisterReq` and `LoginReq` are processed correctly by Spring Boot.
   - Any request missing required fields will automatically return a `400 Bad Request` with validation error messages, handled seamlessly by the existing `GlobalExceptionHandler`.

2. **Rate Limiting Implemented**:
   - Integrated `bucket4j-core` into `pom.xml`.
   - Created [RateLimitingFilter.java](file:///c:/ALL%20ABOUT%20SM/PELATIHAN/LANJUTAN/SRI%20MUTHIA%20NINGRUM/auth_service/src/main/java/com/example/auth/config/RateLimitingFilter.java) to apply rate limiting dynamically.
   - Configured the filter to limit requests starting with `/api/auth` to **20 requests per minute** per IP address.
   - Exceeding the limit results in a `429 Too Many Requests` error response, effectively stopping brute-force spamming.

## Validation Results
- The project successfully compiles with `mvn clean compile` (`BUILD SUCCESS`).
- All the dependencies correctly resolved without any syntax errors.
- The `RateLimitingFilter` successfully attaches to the filter chain as an `OncePerRequestFilter`.

> [!TIP]
> The rate limiting cache uses a standard `ConcurrentHashMap`. If you decide to deploy multiple instances of `auth_service` later (e.g., using Kubernetes or Docker Swarm), you might want to switch to a distributed caching solution for Bucket4j (like Redis) so the limit applies globally across all instances.
