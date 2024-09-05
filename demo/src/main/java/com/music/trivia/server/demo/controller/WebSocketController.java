package com.music.trivia.server.demo.controller;

import com.music.trivia.server.demo.model.User;
import com.music.trivia.server.demo.model.Session;
import com.music.trivia.server.demo.model.TriviaQuestion;
import com.music.trivia.server.demo.service.SessionService;
import com.music.trivia.server.demo.service.TriviaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class WebSocketController {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketController.class);

    private final SessionService sessionService;
    private final TriviaService triviaService;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public WebSocketController(SessionService sessionService, TriviaService triviaService, SimpMessagingTemplate messagingTemplate) {
        this.sessionService = sessionService;
        this.triviaService = triviaService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/userJoin")
    public void handleUserJoin(@Payload Map<String, Object> message) {
        logger.info("Received userJoin message: {}", message);
        String type = (String) message.get("type");
        Map<String, Object> data = (Map<String, Object>) message.get("data");

        if ("USER_JOIN".equals(type)) {
            String userId = (String) data.get("userId");
            String sessionId = (String) data.get("sessionId");
            String avatar = (String) data.get("avatar");
            int score = (int) data.get("score");
            String role = (String) data.get("role");

            Session session = sessionService.getSession(sessionId);

            if (session.getUser(userId) == null) {
                logger.info("Adding user {} to session {}", userId, sessionId);
                User user = new User(avatar, role, score);
                sessionService.addUserToSession(sessionId, userId, user);

                // Broadcast the USER_JOIN message to all users in the session
                Map<String, Object> userJoinMessage = new HashMap<>();
                userJoinMessage.put("type", "USER_JOIN");
                userJoinMessage.put("data", Map.of(
                        "userId", userId,
                        "sessionId", sessionId,
                        "avatar", avatar,
                        "score", score,
                        "role", role
                ));
                logger.info("Broadcasting USER_JOIN message: {}", userJoinMessage);
                messagingTemplate.convertAndSend("/topic/users/" + sessionId, userJoinMessage);
            } else {
                logger.info("User {} already exists in session {}. Sending current user list.", userId, sessionId);
            }

            // Send the current user list to all users in the session
            Map<String, User> users = session.getUsers();
            Map<String, Object> userListMessage = new HashMap<>();
            userListMessage.put("type", "USER_LIST");
            userListMessage.put("data", users);
            messagingTemplate.convertAndSend("/topic/users/" + sessionId, userListMessage);
        }
    }

    @MessageMapping("/userLeave")
    public void handleUserLeave(@Payload Map<String, Object> message) {
        logger.info("Received userLeave message: {}", message);

        Map<String, Object> data = (Map<String, Object>) message.get("data");
        if (data == null) {
            logger.error("Invalid userLeave message. Data is null.");
            return;
        }

        String sessionId = (String) data.get("sessionId");
        String userId = (String) data.get("userId");

        if (sessionId == null || userId == null) {
            logger.error("Invalid userLeave message. SessionId or UserId is null. SessionId: {}, UserId: {}", sessionId, userId);
            return;
        }

        boolean removed = sessionService.leaveUser(sessionId, userId);

        Map<String, Object> leaveMessage = new HashMap<>();
        leaveMessage.put("type", "USER_LEAVE");
        leaveMessage.put("data", Map.of("userId", userId));

        logger.info("Broadcasting USER_LEAVE message: {}", leaveMessage);
        messagingTemplate.convertAndSend("/topic/users/" + sessionId, leaveMessage);

        if (removed) {
            try {
                Session session = sessionService.getSession(sessionId);
                Map<String, User> users = session.getUsers();
                Map<String, Object> userListMessage = new HashMap<>();
                userListMessage.put("type", "USER_LIST");
                userListMessage.put("data", users);
                messagingTemplate.convertAndSend("/topic/users/" + sessionId, userListMessage);
            } catch (IllegalArgumentException e) {
                logger.warn("Failed to send updated user list: {}", e.getMessage());
            }
        }
    }

    @MessageMapping("/userUpdate")
    public void handleUserUpdate(@Payload Map<String, Object> message) {
        logger.info("Received userUpdate message: {}", message);
        String sessionId = (String) message.get("sessionId");
        String userId = (String) message.get("userId");
        Map<String, Object> updates = (Map<String, Object>) message.get("updates");

        sessionService.updateUser(sessionId, userId, updates);

        Map<String, Object> updateMessage = Map.of(
                "type", "USER_UPDATE",
                "data", Map.of(
                        "userId", userId,
                        "updates", updates
                )
        );
        logger.info("Broadcasting USER_UPDATE message: {}", updateMessage);
        messagingTemplate.convertAndSend("/topic/users/" + sessionId, updateMessage);
    }

    @MessageMapping("/startGame")
    public void handleStartGame(@Payload Map<String, Object> message) {
        String sessionId = (String) message.get("sessionId");
        String userId = (String) message.get("userId");

        Session session = sessionService.getSession(sessionId);

        if (session.isCreator(userId)) {
            // Start the game with 10 questions
            triviaService.startGame(sessionId, 10);

            // Broadcast game start to all users in the session
            messagingTemplate.convertAndSend("/topic/game/" + sessionId, Map.of(
                    "type", "GAME_START",
                    "data", Map.of("redirect", "/trivia")
            ));

            // Send first question
            sendNextQuestion(sessionId);
        }
    }

    @MessageMapping("/endGame")
    public void handleEndGame(@Payload Map<String, Object> message) {
        String sessionId = (String) message.get("sessionId");
        String userId = (String) message.get("userId");

        Session session = sessionService.getSession(sessionId);

        if (session.isCreator(userId)) {
            // End the game and reset scores
            triviaService.endGame(sessionId);

            // Get updated scores (all should be zero now)
            Map<String, Integer> updatedScores = triviaService.getScores(sessionId);

            // Create a map of users with their avatars and reset scores
            Map<String, Object> usersData = new HashMap<>();
            for (Map.Entry<String, User> entry : session.getUsers().entrySet()) {
                usersData.put(entry.getKey(), Map.of(
                        "score", 0,
                        "avatar", entry.getValue().getAvatar()
                ));
            }

            // Send end game message with reset scores and user data
            messagingTemplate.convertAndSend("/topic/game/" + sessionId, Map.of(
                    "type", "END_GAME",
                    "data", usersData
            ));

            logger.info("Game ended by creator {} in session {} with all scores reset to zero", userId, sessionId);
        } else {
            logger.warn("Non-creator user {} attempted to end game in session {}", userId, sessionId);
        }
    }

    @MessageMapping("/getCurrentQuestion")
    public void handleGetCurrentQuestion(@Payload Map<String, Object> message) {
        String sessionId = (String) message.get("sessionId");
        TriviaQuestion currentQuestion = triviaService.getCurrentQuestion(sessionId);
        messagingTemplate.convertAndSend("/topic/game/" + sessionId, Map.of(
                "type", "NEW_QUESTION",
                "data", currentQuestion
        ));
    }

    @MessageMapping("/submitAnswer")
    public void handleSubmitAnswer(@Payload Map<String, Object> message) {
        String sessionId = (String) message.get("sessionId");
        String userId = (String) message.get("userId");
        String answer = (String) message.get("answer");

        boolean isCorrect = triviaService.submitAnswer(sessionId, userId, answer);
        User user = sessionService.getSession(sessionId).getUser(userId);

        // Broadcast answer result
        messagingTemplate.convertAndSend("/topic/game/" + sessionId, Map.of(
                "type", "ANSWER_RESULT",
                "data", Map.of(
                        "userId", userId,
                        "isCorrect", isCorrect,
                        "score", triviaService.getScores(sessionId).get(userId),
                        "avatar", user.getAvatar()
                )
        ));

        if (triviaService.isAllUsersAnswered(sessionId)) {
            // Send ALL_ANSWERED message
            messagingTemplate.convertAndSend("/topic/game/" + sessionId, Map.of(
                    "type", "ALL_ANSWERED"
            ));

            // Add a delay before sending the next question
            new Thread(() -> {
                try {
                    Thread.sleep(5000); // 5 seconds delay
                    if (!triviaService.isGameOver(sessionId)) {
                        triviaService.moveToNextQuestion(sessionId);
                        sendNextQuestion(sessionId);
                    } else {
                        // Broadcast game over message
                        messagingTemplate.convertAndSend("/topic/game/" + sessionId, Map.of(
                                "type", "GAME_OVER",
                                "data", triviaService.getScores(sessionId)
                        ));
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }

    @MessageMapping("/nextQuestion")
    public void handleNextQuestion(@Payload Map<String, Object> message) {
        String sessionId = (String) message.get("sessionId");

        if (!triviaService.isGameOver(sessionId)) {
            triviaService.moveToNextQuestion(sessionId);
            sendNextQuestion(sessionId);
        } else {
            // Broadcast game over message
            messagingTemplate.convertAndSend("/topic/game/" + sessionId, Map.of(
                    "type", "GAME_OVER",
                    "data", triviaService.getScores(sessionId)
            ));
        }
    }

    private void sendNextQuestion(String sessionId) {
        TriviaQuestion question = triviaService.getCurrentQuestion(sessionId);
        messagingTemplate.convertAndSend("/topic/game/" + sessionId, Map.of(
                "type", "NEW_QUESTION",
                "data", question
        ));
    }

    @MessageMapping("/removeUser")
    public void handleRemoveUser(@Payload Map<String, Object> message) {
        String sessionId = (String) message.get("sessionId");
        String userToRemove = (String) message.get("userToRemove");
        String requestingUser = (String) message.get("requestingUser");

        boolean removed = sessionService.removeUser(sessionId, userToRemove, requestingUser);
        if (removed) {
            logger.info("User {} removed from session {} by {}", userToRemove, sessionId, requestingUser);
        } else {
            logger.warn("Failed to remove user {} from session {} by {}", userToRemove, sessionId, requestingUser);
        }
    }

    @MessageMapping("/creatorLeave")
    public void handleCreatorLeave(@Payload Map<String, Object> message) {
        String sessionId = (String) message.get("sessionId");
        String creatorId = (String) message.get("creatorId");

        try {
            sessionService.creatorLeave(sessionId, creatorId);
            logger.info("Creator {} left and ended session {}", creatorId, sessionId);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed creator leave attempt for session {} by {}: {}", sessionId, creatorId, e.getMessage());
        }
    }
}