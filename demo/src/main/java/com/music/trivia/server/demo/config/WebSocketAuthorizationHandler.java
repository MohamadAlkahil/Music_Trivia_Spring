package com.music.trivia.server.demo.config;

import com.music.trivia.server.demo.service.JwtService;
import com.music.trivia.server.demo.service.SessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Lazy;

@Component
public class WebSocketAuthorizationHandler implements ChannelInterceptor {

    private final JwtService jwtService;
    private final SessionService sessionService;

    private static final Logger logger = LoggerFactory.getLogger(WebSocketAuthorizationHandler.class);

    @Autowired
    public WebSocketAuthorizationHandler(JwtService jwtService, @Lazy SessionService sessionService) {
        this.jwtService = jwtService;
        this.sessionService = sessionService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("Authorization");
            logger.debug("Received WebSocket connection request with token: {}", token);

            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                String userID = jwtService.extractUsername(token);
                String sessionId = jwtService.extractSessionId(token);

                logger.debug("Extracted userID: {} and sessionId: {} from token", userID, sessionId);

                if (userID != null && sessionId != null && jwtService.isTokenValid(token, userID, sessionId) &&
                        sessionService.isUserInSession(sessionId, userID)) {
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userID, null, null);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    accessor.setUser(auth);
                    logger.info("WebSocket connection authenticated for user: {} in session: {}", userID, sessionId);
                } else {
                    logger.warn("Invalid token or user not in session. User: {}, Session: {}", userID, sessionId);
                }
            } else {
                logger.warn("No valid Authorization header found in WebSocket connection request");
            }
        }

        return message;
    }
}