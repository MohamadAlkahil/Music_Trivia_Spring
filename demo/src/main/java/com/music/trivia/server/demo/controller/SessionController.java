package com.music.trivia.server.demo.controller;

import com.music.trivia.server.demo.exception.UserAlreadyExistsException;
import com.music.trivia.server.demo.exception.UserNotFoundException;
import com.music.trivia.server.demo.model.Session;
import com.music.trivia.server.demo.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/sessions")
public class SessionController {
    private final SessionService sessionService;

    @Autowired
    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping("/create")
    public ResponseEntity<String> createSession(@RequestParam String password) {
        String sessionID = sessionService.createEmptySession(password);
        return ResponseEntity.ok(sessionID);
    }

    @PostMapping("/join")
    public ResponseEntity<String> joinSession(@RequestParam String sessionId, @RequestParam String userID, @RequestParam String password, @RequestParam String avatar) {
        try {
            Session session = sessionService.joinSession(sessionId, userID, password, avatar, "Player");
            return ResponseEntity.ok(userID);
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/kick")
    public ResponseEntity<String> kickUser(
            @RequestParam String sessionId,
            @RequestParam String userID,
            @RequestParam String requestingUser) {
        try {
            boolean isKicked = sessionService.kickUser(sessionId, userID, requestingUser);
            if (isKicked) {
                return ResponseEntity.ok(userID + " has been successfully kicked");
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You don't have permission to kick users");
            }
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/leave")
    public ResponseEntity<String> leaveUser(
            @RequestParam String sessionId,
            @RequestParam String userID) {
        try {
            sessionService.leaveUser(sessionId, userID);
            return ResponseEntity.ok(userID + " has successfully left");
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/score")
    public ResponseEntity<Integer> getUserScore(@RequestParam String sessionId, @RequestParam String userID) {
        try {
            int score = sessionService.getUserScore(sessionId, userID);
            return ResponseEntity.ok(score);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/scoreUpdate")
    public ResponseEntity<String> updateUserScore(@RequestParam String sessionId, @RequestParam String userID, @RequestParam int score) {
        try {
            sessionService.updateUserScore(sessionId, userID, score);
            return ResponseEntity.ok(userID + "'s score has been updated successfully to " + score);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}