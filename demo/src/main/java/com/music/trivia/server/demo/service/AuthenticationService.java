package com.music.trivia.server.demo.service;

import com.music.trivia.server.demo.model.Session;
import com.music.trivia.server.demo.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final SessionService sessionService;
    private final JwtService jwtService;

    public Map<String, String> createSession(String userID, String password) {
        String sessionID = sessionService.createEmptySession(password);
        String token = jwtService.generateToken(userID, sessionID);
        String refreshToken = jwtService.generateRefreshToken(userID, sessionID);

        Map<String, String> response = new HashMap<>();
        response.put("sessionID", sessionID);
        response.put("token", token);
        response.put("refreshToken", refreshToken);

        return response;
    }

    public Map<String, String> joinSession(String sessionId, String userID, String password, String avatar) {
        Session session = sessionService.joinSession(sessionId, userID, password, avatar, "Player");
        String token = jwtService.generateToken(userID, sessionId);
        String refreshToken = jwtService.generateRefreshToken(userID, sessionId);

        Map<String, String> response = new HashMap<>();
        response.put("userID", userID);
        response.put("token", token);
        response.put("refreshToken", refreshToken);

        return response;
    }

    public Map<String, String> refreshToken(String refreshToken) {
        String userID = jwtService.extractUsername(refreshToken);
        String sessionId = jwtService.extractSessionId(refreshToken);

        if (userID != null && sessionId != null && jwtService.isTokenValid(refreshToken, userID, sessionId)) {
            String newToken = jwtService.generateToken(userID, sessionId);
            String newRefreshToken = jwtService.generateRefreshToken(userID, sessionId);

            Map<String, String> response = new HashMap<>();
            response.put("token", newToken);
            response.put("refreshToken", newRefreshToken);

            return response;
        }

        throw new IllegalArgumentException("Invalid refresh token");
    }
}