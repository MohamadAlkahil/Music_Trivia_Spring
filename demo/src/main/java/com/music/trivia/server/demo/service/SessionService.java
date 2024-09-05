package com.music.trivia.server.demo.service;

import com.music.trivia.server.demo.exception.UserAlreadyExistsException;
import com.music.trivia.server.demo.exception.UserNotFoundException;
import com.music.trivia.server.demo.model.Session;
import com.music.trivia.server.demo.model.SessionStorage;
import com.music.trivia.server.demo.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class SessionService {
    private static final Logger logger = LoggerFactory.getLogger(SessionService.class);

    private final SessionStorage sessionStorage;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public SessionService(SessionStorage sessionStorage, SimpMessagingTemplate messagingTemplate) {
        this.sessionStorage = sessionStorage;
        this.messagingTemplate = messagingTemplate;
    }

    public String createEmptySession(String password) {
        String sessionID = java.util.UUID.randomUUID().toString();
        Session session = new Session(sessionID, password);
        sessionStorage.storeSession(sessionID, session);
        logger.info("Created new empty session with ID: {}", sessionID);
        return sessionID;
    }

    public Session joinSession(String sessionId, String userID, String password, String avatar, String role) {
        Session session = sessionStorage.getSession(sessionId);
        if (session.getPassword().equals(password)) {
            User user = new User(avatar, role, 0);
            session.addUser(userID, user);
            logger.info("User {} joined session {}", userID, sessionId);
            return session;
        } else {
            logger.warn("Failed join attempt for session {} with incorrect password", sessionId);
            throw new IllegalArgumentException("Invalid session ID or password");
        }
    }

    public void addUserToSession(String sessionId, String userId, User user) {
        Session session = sessionStorage.getSession(sessionId);
        if (session.getUser(userId) == null) {
            session.addUser(userId, user);
            logger.info("Added user {} to session {}", userId, sessionId);
        } else {
            logger.info("User {} already exists in session {}", userId, sessionId);
        }
    }

    public void removeUserFromSession(String sessionId, String userId) {
        Session session = sessionStorage.getSession(sessionId);
        session.removeUser(userId);
        logger.info("Removed user {} from session {}", userId, sessionId);
    }

    public void updateUser(String sessionId, String userId, Map<String, Object> updates) {
        Session session = sessionStorage.getSession(sessionId);
        User user = session.getUser(userId);
        if (user != null) {
            if (updates.containsKey("avatar")) {
                user.setAvatar((String) updates.get("avatar"));
            }
            if (updates.containsKey("score")) {
                user.setScore((Integer) updates.get("score"));
            }
            if (updates.containsKey("role")) {
                user.setRole((String) updates.get("role"));
            }
            logger.info("Updated user {} in session {}: {}", userId, sessionId, updates);
        } else {
            logger.warn("Attempted to update non-existent user {} in session {}", userId, sessionId);
        }
    }

    public boolean kickUser(String sessionId, String userId, String requestingUser) {
        Session session = sessionStorage.getSession(sessionId);
        if (session.isCreator(requestingUser)) {
            session.removeUser(userId);
            logger.info("User {} kicked from session {} by creator {}", userId, sessionId, requestingUser);
            return true;
        }
        logger.warn("Failed attempt to kick user {} from session {} by non-creator {}", userId, sessionId, requestingUser);
        return false;
    }

    public Map<String, User> getUsersInSession(String sessionId) {
        Session session = sessionStorage.getSession(sessionId);
        return session.getUsers();
    }

    public Integer getUserScore(String sessionId, String userId) {
        Session session = sessionStorage.getSession(sessionId);
        Integer score = session.getUserScore(userId);
        logger.info("Retrieved score {} for user {} in session {}", score, userId, sessionId);
        return score;
    }

    public void updateUserScore(String sessionId, String userId, int score) {
        Session session = sessionStorage.getSession(sessionId);
        session.updateUserScore(userId, score);
        logger.info("Updated score to {} for user {} in session {}", score, userId, sessionId);
    }

    public boolean leaveUser(String sessionId, String userID) {
        try {
            Session session = sessionStorage.getSession(sessionId);
            if (session != null) {
                session.removeUser(userID);
                logger.info("User {} left session {}", userID, sessionId);
                return true;
            } else {
                logger.warn("Session {} not found when attempting to remove user {}", sessionId, userID);
                return false;
            }
        } catch (IllegalArgumentException e) {
            logger.warn("Error removing user {} from session {}: {}", userID, sessionId, e.getMessage());
            return false;
        }
    }

    public Session getSession(String sessionId) {
        try {
            Session session = sessionStorage.getSession(sessionId);
            logger.info("Retrieved session {}", sessionId);
            return session;
        } catch (IllegalArgumentException e) {
            logger.warn("Session {} not found", sessionId);
            throw e;
        }
    }

    public boolean removeUser(String sessionId, String userId, String requestingUser) {
        Session session = sessionStorage.getSession(sessionId);
        if (session.isCreator(requestingUser)) {
            session.removeUser(userId);
            logger.info("User {} removed from session {} by creator {}", userId, sessionId, requestingUser);

            // Broadcast user removal to all users in the session
            messagingTemplate.convertAndSend("/topic/users/" + sessionId, Map.of(
                    "type", "USER_REMOVED",
                    "data", Map.of("userId", userId)
            ));

            // Send a direct message to the removed user
            messagingTemplate.convertAndSendToUser(userId, "/queue/errors", Map.of(
                    "type", "REMOVED_FROM_SESSION",
                    "data", Map.of("sessionId", sessionId)
            ));

            return true;
        }
        logger.warn("Failed attempt to remove user {} from session {} by non-creator {}", userId, sessionId, requestingUser);
        return false;
    }

    public void creatorLeave(String sessionId, String creatorId) {
        Session session = sessionStorage.getSession(sessionId);
        if (session.isCreator(creatorId)) {
            // Remove all users and broadcast
            for (String userId : session.getUsers().keySet()) {
                if (!userId.equals(creatorId)) {
                    session.removeUser(userId);
                    messagingTemplate.convertAndSendToUser(userId, "/queue/errors", Map.of(
                            "type", "SESSION_ENDED",
                            "data", Map.of("sessionId", sessionId)
                    ));
                }
            }

            // Broadcast session end to all users
            messagingTemplate.convertAndSend("/topic/users/" + sessionId, Map.of(
                    "type", "SESSION_ENDED",
                    "data", Map.of("sessionId", sessionId)
            ));

            // Remove the session
            sessionStorage.removeSession(sessionId);
            logger.info("Session {} ended by creator {}", sessionId, creatorId);
        } else {
            logger.warn("Non-creator {} attempted to end session {}", creatorId, sessionId);
            throw new IllegalArgumentException("Only the creator can end the session");
        }
    }
}