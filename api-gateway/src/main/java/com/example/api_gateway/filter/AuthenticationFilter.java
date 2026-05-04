package com.example.api_gateway.filter;

import com.example.api_gateway.config.RouteValidator;
import com.example.api_gateway.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {
    private static final String INTERNAL_TRAFFIC_HEADER = "X-Internal-Traffic-Secret";
    private static final String INTERNAL_TRAFFIC_SECRET = "demo-secure-key-2026";


    @Autowired
    private RouteValidator validator;
    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        if (validator.isSecured.test(request)) {

            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, "Missing authorization header", HttpStatus.UNAUTHORIZED);
            }

            List<String> authHeaders = request.getHeaders().get(HttpHeaders.AUTHORIZATION);
            if (authHeaders == null || authHeaders.isEmpty()) {
                return onError(exchange, "Missing authorization header", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = authHeaders.get(0);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                authHeader = authHeader.substring(7);
            } else {
                return onError(exchange, "Invalid authorization header format", HttpStatus.UNAUTHORIZED);
            }

            try {
                jwtUtil.validateToken(authHeader);


                String userId = jwtUtil.extractUserId(authHeader);


                request = exchange.getRequest()
                        .mutate()
                        .header("X-User-Id", userId)
                        .build();

            } catch (Exception e) {

                return onError(exchange, "Unauthorized access to application", HttpStatus.UNAUTHORIZED);
            }
        }

        request = request.mutate()
                .header(INTERNAL_TRAFFIC_HEADER, INTERNAL_TRAFFIC_SECRET)
                .build();

        return chain.filter(exchange.mutate().request(request).build());
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }

    @Override
    public int getOrder() {
        return -1;
    }
}