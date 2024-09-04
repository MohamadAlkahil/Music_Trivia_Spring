package com.music.trivia.server.demo.model;

import com.music.trivia.server.demo.exception.UserAlreadyExistsException;
import com.music.trivia.server.demo.exception.UserNotFoundException;
import lombok.Data;

import java.time.Instant;
import java.util.*;

@Data
public class Session {
    private String sessionID;
    private String password;
    private Map<String, User> users;
    private Instant creationTime;
    private List<TriviaQuestion> triviaQuestions;
    private int currentQuestionIndex;
    private Set<String> answeredUsers;

    public Session(String sessionID, String password) {
        this.sessionID = sessionID;
        this.password = password;
        this.users = new HashMap<>();
        this.creationTime = Instant.now();
        this.answeredUsers = new HashSet<>();
    }

    public void addUser(String userID, User user) {
        if (users.containsKey(userID)) {
            throw new UserAlreadyExistsException("User with ID " + userID + " already exists in the session");
        } else {
            users.put(userID, user);
        }
    }

    public void removeUser(String userID) {
        if (!users.containsKey(userID)) {
            throw new UserNotFoundException("User with ID " + userID + " not found in the session");
        } else {
            users.remove(userID);
        }
    }

    public User getUser(String userID) {
        return users.get(userID);
    }

    public Integer getUserScore(String userID) {
        User user = users.get(userID);
        if (user == null) {
            throw new UserNotFoundException("User with ID " + userID + " not found in the session");
        }
        return user.getScore();
    }

    public void updateUserScore(String userID, int score) {
        User user = users.get(userID);
        if (user == null) {
            throw new UserNotFoundException("User with ID " + userID + " not found in the session");
        } else {
            user.setScore(score);
        }
    }

    public boolean isCreator(String userID) {
        User user = users.get(userID);
        return user != null && "Creator".equals(user.getRole());
    }

    public Map<String, User> getUsers() {
        return new HashMap<>(users);
    }

    public TriviaQuestion getCurrentQuestion() {
        return triviaQuestions.get(currentQuestionIndex);
    }

    public void moveToNextQuestion() {
        currentQuestionIndex++;
    }

    public boolean isGameOver() {
        return currentQuestionIndex >= triviaQuestions.size();
    }

    public void addAnsweredUser(String userID) {
        answeredUsers.add(userID);
    }

    public boolean isAllUsersAnswered() {
        return answeredUsers.size() == users.size();
    }

    public void resetAnsweredUsers() {
        answeredUsers.clear();
    }

    public Map<String, Integer> getScores() {
        Map<String, Integer> scores = new HashMap<>();
        for (Map.Entry<String, User> entry : users.entrySet()) {
            scores.put(entry.getKey(), entry.getValue().getScore());
        }
        return scores;
    }
}