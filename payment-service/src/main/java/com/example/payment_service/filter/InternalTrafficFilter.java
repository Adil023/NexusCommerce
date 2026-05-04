package com.example.payment_service.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

@Component
public class InternalTrafficFilter extends OncePerRequestFilter {

    private static final String INTERNAL_TRAFFIC_HEADER = "X-Internal-Traffic-Secret";

    @Value("${internal.traffic.secret}")
    private String internalTrafficSecret;

    private static final List<String> SWAGGER_PATH_PREFIXES = List.of(
            "/v3/api-docs",
            "/swagger-ui"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return "/swagger-ui.html".equals(path)
                || SWAGGER_PATH_PREFIXES.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String secret = request.getHeader(INTERNAL_TRAFFIC_HEADER);
        if (!internalTrafficSecret.equals(secret)) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            return;
        }
        filterChain.doFilter(request, response);
    }
}