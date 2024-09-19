package com.music.trivia.server.demo.controller;

import com.music.trivia.server.demo.exception.UserAlreadyExistsException;
import com.music.trivia.server.demo.exception.UserNotFoundException;
import com.music.trivia.server.demo.model.Session;
import com.music.trivia.server.demo.service.SessionService;
import com.music.trivia.server.demo.service.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/sessions")
public class SessionController {
    private static final Logger logger = LoggerFactory.getLogger(SessionController.class);
    private final SessionService sessionService;
    private final JwtService jwtService;

    @Autowired
    public SessionController(SessionService sessionService, JwtService jwtService) {
        this.sessionService = sessionService;
        this.jwtService = jwtService;
    }

    @PostMapping("/create")
    public ResponseEntity<Map<String, String>> createSession(@RequestParam String userID, @RequestParam String password) {
        logger.info("Received request to create session for user: {}", userID);
        try {
            String sessionID = sessionService.createEmptySession(password);
            String token = jwtService.generateToken(userID, sessionID);
            String refreshToken = jwtService.generateRefreshToken(userID, sessionID);

            Map<String, String> response = new HashMap<>();
            response.put("sessionID", sessionID);
            response.put("token", token);
            response.put("refreshToken", refreshToken);

            logger.info("Session created successfully for user: {}, sessionID: {}", userID, sessionID);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error creating session for user: {}", userID, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/join")
    public ResponseEntity<Map<String, String>> joinSession(@RequestParam String sessionId, @RequestParam String userID, @RequestParam String password, @RequestParam String avatar) {
        logger.info("Received request to join session: {} for user: {}", sessionId, userID);
        try {
            Session session = sessionService.joinSession(sessionId, userID, password, avatar, "Player");
            String token = jwtService.generateToken(userID, sessionId);
            String refreshToken = jwtService.generateRefreshToken(userID, sessionId);

            Map<String, String> response = new HashMap<>();
            response.put("userID", userID);
            response.put("token", token);
            response.put("refreshToken", refreshToken);

            logger.info("User: {} successfully joined session: {}", userID, sessionId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error joining session: {} for user: {}", sessionId, userID, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<Map<String, String>> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        logger.info("Received request to refresh token");
        try {
            String userID = jwtService.extractUsername(refreshToken);
            String sessionId = jwtService.extractSessionId(refreshToken);

            if (userID != null && sessionId != null && jwtService.isTokenValid(refreshToken, userID, sessionId)) {
                String newToken = jwtService.generateToken(userID, sessionId);
                String newRefreshToken = jwtService.generateRefreshToken(userID, sessionId);

                Map<String, String> response = new HashMap<>();
                response.put("token", newToken);
                response.put("refreshToken", newRefreshToken);

                logger.info("Token refreshed successfully for user: {}, session: {}", userID, sessionId);
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Invalid refresh token received");
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid refresh token"));
            }
        } catch (Exception e) {
            logger.error("Error refreshing token", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

}