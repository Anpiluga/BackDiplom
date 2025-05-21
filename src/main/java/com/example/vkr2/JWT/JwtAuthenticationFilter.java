package com.example.vkr2.JWT;

import com.example.vkr2.JWT.services.JwtService;
import com.example.vkr2.JWT.services.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    public static final String BEARER_PREFIX = "Bearer ";
    public static final String HEADER_NAME = "Authorization";
    private final JwtService jwtService;
    private final UserService userService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        logger.debug("Processing request: {} {}", method, requestURI);

        // Получаем токен из заголовка
        var authHeader = request.getHeader(HEADER_NAME);
        if (StringUtils.isEmpty(authHeader) || !StringUtils.startsWith(authHeader, BEARER_PREFIX)) {
            logger.debug("No Bearer token found in request to {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Обрезаем префикс и получаем имя пользователя из токена
            var jwt = authHeader.substring(BEARER_PREFIX.length());
            logger.debug("Extracted JWT token for request to {}", requestURI);

            var username = jwtService.extractUserName(jwt);
            logger.debug("Extracted username: {} for request to {}", username, requestURI);

            if (StringUtils.isNotEmpty(username) && SecurityContextHolder.getContext().getAuthentication() == null) {
                try {
                    UserDetails userDetails = userService
                            .userDetailsService()
                            .loadUserByUsername(username);

                    logger.debug("Loaded user details for: {}, authorities: {}", username, userDetails.getAuthorities());

                    // Если токен валиден, то аутентифицируем пользователя
                    if (jwtService.isTokenValid(jwt, userDetails)) {
                        SecurityContext context = SecurityContextHolder.createEmptyContext();

                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        context.setAuthentication(authToken);
                        SecurityContextHolder.setContext(context);

                        logger.debug("Successfully authenticated user: {} for request: {} {}", username, method, requestURI);
                    } else {
                        logger.warn("Invalid JWT token for user: {} on request: {} {}", username, method, requestURI);
                    }
                } catch (Exception e) {
                    logger.error("Error loading user details for username: {} on request: {} {}", username, method, requestURI, e);
                }
            }
        } catch (Exception e) {
            logger.error("Error processing JWT token for request: {} {}", method, requestURI, e);
        }

        filterChain.doFilter(request, response);
    }
}