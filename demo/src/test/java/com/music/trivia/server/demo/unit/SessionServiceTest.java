package com.music.trivia.server.demo.unit;

import com.music.trivia.server.demo.service.SessionService;
import com.music.trivia.server.demo.exception.UserAlreadyExistsException;
import com.music.trivia.server.demo.exception.UserNotFoundException;
import com.music.trivia.server.demo.model.Session;
import com.music.trivia.server.demo.model.SessionStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class SessionServiceTest {
//
//    @Mock
//    private SessionStorage sessionStorage;
//
//    @InjectMocks
//    private SessionService sessionService;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    void createSession_success() {
//        String userID = "user1";
//        String password = "password";
//        String sessionID = "session1";
//
//        when(sessionStorage.createSession(anyString(), anyString())).thenReturn(sessionID);
//
//        String result = sessionService.createSession(userID, password);
//
//        assertEquals(sessionID, result);
//        verify(sessionStorage, times(1)).createSession(userID, password);
//    }
//
//    @Test
//    void joinSession_success() {
//        // Arrange
//        String sessionId = "session1";
//        String userID = "user1";
//        String password = "password";
//        Session session = new Session(sessionId, userID, password);
//
//        // Stub behavior of getSession() to return the pre-configured session
//        when(sessionStorage.getSession(sessionId)).thenReturn(session);
//
//        // Act + Assert
//        assertThrows(UserAlreadyExistsException.class,
//                () -> sessionService.joinSession(sessionId, userID, password));
//
//        // Optional: Reset session state after test
//        session = new Session(sessionId, userID, password);
//    }
//

//    @Test
//    void kickUser_success() {
//        // Arrange
//        String sessionId = "session1";
//        String userID = "user1";
//        Session session = new Session(sessionId, userID, "password");
//
//        // Stub behavior of getSession() to return the pre-configured session
//        when(sessionStorage.getSession(sessionId)).thenReturn(session);
//
//        // Act + Assert
//        assertDoesNotThrow(() -> sessionService.kickUser(sessionId, userID));
//        verify(sessionStorage, times(1)).getSession(sessionId);
//    }
//
//    @Test
//    void kickUser_userNotFound() {
//        // Arrange
//        String sessionId = "session1";
//        String userID = "user1";
//        Session session = new Session(sessionId, "anotherUser", "password");
//
//        // Stub behavior of getSession() to return the pre-configured session
//        when(sessionStorage.getSession(sessionId)).thenReturn(session);
//
//        // Set up mock behavior to throw UserNotFoundException
//        doThrow(new UserNotFoundException("User not found")).when(sessionStorage).getSession(sessionId);
//
//        // Act + Assert
//        assertThrows(UserNotFoundException.class, () -> sessionService.kickUser(sessionId, userID));
//        verify(sessionStorage, times(1)).getSession(sessionId);
//    }
//
//    @Test
//    void getUserScore_success() {
//        // Arrange
//        String sessionId = "session1";
//        String userID = "user1";
//        int score = 10;
//        Session session = new Session(sessionId, userID, "password");
//        session.updateUserScore(userID, score);
//
//        // Stub behavior of getSession() to return the pre-configured session
//        when(sessionStorage.getSession(sessionId)).thenReturn(session);
//
//        // Act
//        int result = sessionService.getUserScore(sessionId, userID);
//
//        // Assert
//        assertEquals(score, result);
//        verify(sessionStorage, times(1)).getSession(sessionId);
//    }
//
//    @Test
//    void getUserScore_userNotFound() {
//        // Arrange
//        String sessionId = "session1";
//        String userID = "user1";
//        Session session = new Session(sessionId, "anotherUser", "password");
//
//        // Stub behavior of getSession() to return the pre-configured session
//        when(sessionStorage.getSession(sessionId)).thenReturn(session);
//
//        // Set up mock behavior to throw UserNotFoundException
//        doThrow(new UserNotFoundException("User not found")).when(sessionStorage).getSession(sessionId);
//
//        // Act + Assert
//        assertThrows(UserNotFoundException.class, () -> sessionService.getUserScore(sessionId, userID));
//        verify(sessionStorage, times(1)).getSession(sessionId);
//    }
//
//    @Test
//    void updateUserScore_success() {
//        // Arrange
//        String sessionId = "session1";
//        String userID = "user1";
//        int score = 20;
//        Session session = new Session(sessionId, userID, "password");
//
//        // Stub behavior of getSession() to return the pre-configured session
//        when(sessionStorage.getSession(sessionId)).thenReturn(session);
//
//        // Act + Assert
//        assertDoesNotThrow(() -> sessionService.updateUserScore(sessionId, userID, score));
//        verify(sessionStorage, times(1)).getSession(sessionId);
//    }
//
//    @Test
//    void updateUserScore_userNotFound() {
//        // Arrange
//        String sessionId = "session1";
//        String userID = "user1";
//        int score = 20;
//        Session session = new Session(sessionId, "anotherUser", "password");
//
//        // Stub behavior of getSession() to return the pre-configured session
//        when(sessionStorage.getSession(sessionId)).thenReturn(session);
//
//        // Set up mock behavior to throw UserNotFoundException
//        doThrow(new UserNotFoundException("User not found")).when(sessionStorage).getSession(sessionId);
//
//        // Act + Assert
//        assertThrows(UserNotFoundException.class, () -> sessionService.updateUserScore(sessionId, userID, score));
//        verify(sessionStorage, times(1)).getSession(sessionId);
//    }
}
