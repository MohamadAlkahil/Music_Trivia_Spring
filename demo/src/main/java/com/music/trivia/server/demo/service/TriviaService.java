package com.music.trivia.server.demo.service;

import com.music.trivia.server.demo.model.Session;
import com.music.trivia.server.demo.model.TriviaQuestion;
import com.music.trivia.server.demo.model.User;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class TriviaService {
    private static final Logger logger = LoggerFactory.getLogger(TriviaService.class);
    private static final int QUESTION_TIME_LIMIT = 60; // 60 seconds

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SessionService sessionService;
    private final SimpMessagingTemplate messagingTemplate;
    private final Map<String, Integer> sessionTimers = new ConcurrentHashMap<>();

    @Autowired
    public TriviaService(SessionService sessionService, SimpMessagingTemplate messagingTemplate) {
        this.sessionService = sessionService;
        this.messagingTemplate = messagingTemplate;
    }

    public List<TriviaQuestion> fetchMusicTrivia(int numberOfQuestions) {
        logger.info("Fetching {} music trivia questions", numberOfQuestions);
        String url = "https://opentdb.com/api.php?amount=" + numberOfQuestions + "&category=12&type=multiple";
        String response = restTemplate.getForObject(url, String.class);
        List<TriviaQuestion> questions = new ArrayList<>();

        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode results = root.path("results");
            logger.info("Received {} questions from API", results.size());

            for (JsonNode result : results) {
                String question = StringEscapeUtils.unescapeHtml4(result.path("question").asText());
                String correctAnswer = StringEscapeUtils.unescapeHtml4(result.path("correct_answer").asText());
                List<String> incorrectAnswers = new ArrayList<>();
                for (JsonNode incorrect : result.path("incorrect_answers")) {
                    incorrectAnswers.add(StringEscapeUtils.unescapeHtml4(incorrect.asText()));
                }
                questions.add(new TriviaQuestion(question, correctAnswer, incorrectAnswers));
            }
        } catch (Exception e) {
            logger.error("Error parsing trivia API response", e);
            throw new RuntimeException("Error parsing trivia API response", e);
        }

        logger.info("Successfully parsed {} questions", questions.size());
        return questions;
    }

    public void setTriviaQuestions(String sessionId, List<TriviaQuestion> questions) {
        logger.info("Setting {} trivia questions for session {}", questions.size(), sessionId);
        Session session = sessionService.getSession(sessionId);
        session.setTriviaQuestions(questions);
        session.setCurrentQuestionIndex(0);
        session.resetAnsweredUsers();
        logger.info("Trivia questions set successfully for session {}", sessionId);
    }

    public TriviaQuestion getCurrentQuestion(String sessionId) {
        logger.info("Getting current question for session {}", sessionId);
        Session session = sessionService.getSession(sessionId);
        TriviaQuestion question = session.getCurrentQuestion();
        if (question != null) {
            logger.info("Retrieved current question for session {}: {}", sessionId, question.getQuestion());
        } else {
            logger.warn("No current question available for session {}", sessionId);
        }
        return question;
    }

    public void startGame(String sessionId, int numberOfQuestions) {
        List<TriviaQuestion> questions = fetchMusicTrivia(numberOfQuestions);
        setTriviaQuestions(sessionId, questions);
        logger.info("Started new game for session {} with {} questions", sessionId, numberOfQuestions);
        sendNextQuestion(sessionId);
    }

    public void sendNextQuestion(String sessionId) {
        logger.info("Preparing to send next question for session {}", sessionId);
        Session session = sessionService.getSession(sessionId);
        TriviaQuestion question = session.getCurrentQuestion();
        if (question == null) {
            logger.warn("No question available for session: {}", sessionId);
            return;
        }
        logger.info("Sending next question to session: {}", sessionId);
        Map<String, Object> message = new HashMap<>();
        message.put("type", "NEW_QUESTION");
        message.put("data", question);
        messagingTemplate.convertAndSend("/topic/game/" + sessionId, message);
        logger.info("Sent NEW_QUESTION message for session {}: {}", sessionId, message);
    }

    public boolean submitAnswer(String sessionId, String userId, String answer) {
        Session session = sessionService.getSession(sessionId);
        TriviaQuestion currentQuestion = session.getCurrentQuestion();
        boolean isCorrect = currentQuestion.getCorrectAnswer().equals(answer);
        if (isCorrect) {
            int currentScore = session.getUserScore(userId);
            session.updateUserScore(userId, currentScore + 1);
            logger.info("User {} answered correctly in session {}", userId, sessionId);
        } else {
            logger.info("User {} answered incorrectly in session {}", userId, sessionId);
        }
        session.addAnsweredUser(userId);


        return isCorrect;
    }

    public boolean isAllUsersAnswered(String sessionId) {
        Session session = sessionService.getSession(sessionId);
        return session.isAllUsersAnswered();
    }

    public boolean isGameOver(String sessionId) {
        Session session = sessionService.getSession(sessionId);
        return session.isGameOver();
    }

    public Map<String, Integer> getScores(String sessionId) {
        Session session = sessionService.getSession(sessionId);
        return session.getScores();
    }

    public void endGame(String sessionId) {
        Session session = sessionService.getSession(sessionId);
        session.setTriviaQuestions(null);
        session.setCurrentQuestionIndex(0);
        session.resetAnsweredUsers();

        // Reset all player scores to zero
        for (Map.Entry<String, User> entry : session.getUsers().entrySet()) {
            String userId = entry.getKey();
            User user = entry.getValue();
            user.setScore(0);
            sessionService.updateUserScore(sessionId, userId, 0);
        }

        logger.info("Game ended for session {} with all scores reset to zero", sessionId);
    }

    public void startTimer(String sessionId) {
        sessionTimers.put(sessionId, QUESTION_TIME_LIMIT);
    }

    @Scheduled(fixedRate = 1000) // Run every second
    public void updateTimers() {
        for (Map.Entry<String, Integer> entry : sessionTimers.entrySet()) {
            String sessionId = entry.getKey();
            int timeLeft = entry.getValue();

            if (timeLeft > 0) {
                timeLeft--;
                sessionTimers.put(sessionId, timeLeft);

                // Send timer update to clients
                messagingTemplate.convertAndSend("/topic/game/" + sessionId, Map.of(
                        "type", "TIMER_UPDATE",
                        "data", Map.of("timeLeft", timeLeft)
                ));
            } else {
                // Time's up, force move to next question
                sessionTimers.remove(sessionId);
                if (!isGameOver(sessionId)) {
                    moveToNextQuestion(sessionId);
                    sendNextQuestion(sessionId);
                } else {
                    endGame(sessionId);
                }
            }
        }
    }

    public void moveToNextQuestion(String sessionId) {
        Session session = sessionService.getSession(sessionId);
        session.moveToNextQuestion();
        session.resetAnsweredUsers();
        startTimer(sessionId);
    }
}