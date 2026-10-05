package com.example.api_gateway.filter;

import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
    }

    public static class Config {
        // configuration properties
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();

            // Skip authentication for specific endpoints
            if ((path.startsWith("/api/rooms") && request.getMethod() != null && request.getMethod().name().equals("GET")) ||
                path.startsWith("/api/auth/login") || 
                path.startsWith("/api/auth/register") || 
                path.startsWith("/api/auth/refresh-token") || 
                path.startsWith("/api/payments/midtrans/callback") ||
                path.startsWith("/v3/api-docs") || 
                path.startsWith("/swagger-ui") ||
                path.startsWith("/uploads/") ||
                path.startsWith("/payment/") ||
                path.startsWith("/css/") ||
                path.startsWith("/js/") ||
                path.startsWith("/images/") ||
                path.equals("/") ||
                path.equals("/login") ||
                path.equals("/register") ||
                path.startsWith("/dashboard") ||
                path.startsWith("/admin/") ||
                path.startsWith("/tenant/") ||
                path.startsWith("/qris-simulate") ||
                (path.startsWith("/api/payments/") && (path.endsWith("/qris") || path.endsWith("/qris-pay") || request.getMethod().name().equals("GET")))) {
                return chain.filter(exchange);
            }

            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return this.onError(exchange, "No Authorization header", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                authHeader = authHeader.substring(7);
            } else {
                return this.onError(exchange, "Invalid Authorization header", HttpStatus.UNAUTHORIZED);
            }

            try {
                if (jwtUtil.isInvalid(authHeader)) {
                    return this.onError(exchange, "Token is expired or invalid", HttpStatus.UNAUTHORIZED);
                }

                Claims claims = jwtUtil.getClaims(authHeader);
                String userId = claims.get("userId", String.class);
                String role = claims.get("role", String.class);
                String username = claims.getSubject();

                ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                        .header("X-USER-ID", userId)
                        .header("X-USER-ROLE", role)
                        .header("X-USER-EMAIL", username)
                        .build();

                return chain.filter(exchange.mutate().request(modifiedRequest).build());

            } catch (Exception e) {
                return this.onError(exchange, "Token validation failed", HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }
}
