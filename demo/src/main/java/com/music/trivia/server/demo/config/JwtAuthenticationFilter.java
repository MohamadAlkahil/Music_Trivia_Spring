package com.music.trivia.server.demo.config;

import com.music.trivia.server.demo.service.JwtService;
import com.music.trivia.server.demo.service.SessionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final SessionService sessionService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userID;
        final String sessionId;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.debug("No JWT token found in request headers");
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        userID = jwtService.extractUsername(jwt);
        sessionId = jwtService.extractSessionId(jwt);

        logger.debug("JWT token found in request headers");

        if (userID != null && sessionId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            logger.debug("Validating token for user: {} in session: {}", userID, sessionId);
            if (jwtService.isTokenValid(jwt, userID, sessionId) && sessionService.isUserInSession(sessionId, userID)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userID,
                        null,
                        null // You might want to add authorities here if needed
                );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);
                logger.info("User: {} authenticated for session: {}", userID, sessionId);
            } else {
                logger.warn("Invalid token or user not in session. User: {}, Session: {}", userID, sessionId);
            }
        }
        filterChain.doFilter(request, response);
    }
}