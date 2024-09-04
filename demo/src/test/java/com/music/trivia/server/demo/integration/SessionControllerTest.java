package com.music.trivia.server.demo.integration;
import com.music.trivia.server.demo.controller.SessionController;
import com.music.trivia.server.demo.exception.UserAlreadyExistsException;
import com.music.trivia.server.demo.exception.UserNotFoundException;
import com.music.trivia.server.demo.service.SessionService;
import com.music.trivia.server.demo.config.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SessionController.class)
@ContextConfiguration(classes = {TestSecurityConfig.class, SessionController.class})
class SessionControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockBean
//    private SessionService sessionService;
//
//    @Test
//    void createSession_success() throws Exception {
//        String userID = "user1";
//        String password = "password";
//        String sessionID = "session1";
//
//        Mockito.when(sessionService.createSession(userID, password)).thenReturn(sessionID);
//
//        mockMvc.perform(post("/api/sessions/create")
//                        .param("userID", userID)
//                        .param("password", password))
//                .andExpect(status().isOk())
//                .andExpect(content().string("Session with ID " + sessionID + " has been created"));
//    }
//
//    @Test
//    void joinSession_success() throws Exception {
//        String sessionId = "session1";
//        String userID = "user1";
//        String password = "password";
//
//        mockMvc.perform(post("/api/sessions/join")
//                        .param("sessionId", sessionId)
//                        .param("userID", userID)
//                        .param("password", password))
//                .andExpect(status().isOk())
//                .andExpect(content().string(userID + " has joined successfully"));
//    }
//
//    @Test
//    void joinSession_userAlreadyExists() throws Exception {
//        String sessionId = "session1";
//        String userID = "user1";
//        String password = "password";
//
//        Mockito.doThrow(new UserAlreadyExistsException("User already exists"))
//                .when(sessionService).joinSession(sessionId, userID, password);
//
//        mockMvc.perform(post("/api/sessions/join")
//                        .param("sessionId", sessionId)
//                        .param("userID", userID)
//                        .param("password", password))
//                .andExpect(status().isConflict())
//                .andExpect(content().string("User already exists"));
//    }
//
//    @Test
//    void kickUser_success() throws Exception {
//        String sessionId = "session1";
//        String userID = "user1";
//
//        mockMvc.perform(post("/api/sessions/kick")
//                        .param("sessionId", sessionId)
//                        .param("userID", userID))
//                .andExpect(status().isOk())
//                .andExpect(content().string(userID + " has been successfully kicked "));
//    }
//
////    @Test
////    void kickUser_userNotFound() throws Exception {
////        String sessionId = "session1";
////        String userID = "user1";
////
////        Mockito.doThrow(new UserNotFoundException("User not found"))
////                .when(sessionService).kickUser(sessionId, userID);
////
////        mockMvc.perform(post("/api/sessions/kick")
////                        .param("sessionId", sessionId)
////                        .param("userID", userID))
////                .andExpect(status().isNotFound())
////                .andExpect(content().string("User not found"));
////    }
//
//    @Test
//    void getUserScore_success() throws Exception {
//        String sessionId = "session1";
//        String userID = "user1";
//        int score = 10;
//
//        Mockito.when(sessionService.getUserScore(sessionId, userID)).thenReturn(score);
//
//        mockMvc.perform(get("/api/sessions/score")
//                        .param("sessionId", sessionId)
//                        .param("userID", userID))
//                .andExpect(status().isOk())
//                .andExpect(content().string(String.valueOf(score)));
//    }
//
//    @Test
//    void getUserScore_userNotFound() throws Exception {
//        String sessionId = "session1";
//        String userID = "user1";
//
//        Mockito.doThrow(new UserNotFoundException("User not found"))
//                .when(sessionService).getUserScore(sessionId, userID);
//
//        mockMvc.perform(get("/api/sessions/score")
//                        .param("sessionId", sessionId)
//                        .param("userID", userID))
//                .andExpect(status().isNotFound());
//    }
//
//    @Test
//    void updateUserScore_success() throws Exception {
//        String sessionId = "session1";
//        String userID = "user1";
//        int score = 20;
//
//        mockMvc.perform(post("/api/sessions/scoreUpdate")
//                        .param("sessionId", sessionId)
//                        .param("userID", userID)
//                        .param("score", String.valueOf(score)))
//                .andExpect(status().isOk())
//                .andExpect(content().string(userID + "'s score has been updated successfully to " + score));
//    }
//
//    @Test
//    void updateUserScore_userNotFound() throws Exception {
//        String sessionId = "session1";
//        String userID = "user1";
//        int score = 20;
//
//        Mockito.doThrow(new UserNotFoundException("User not found"))
//                .when(sessionService).updateUserScore(sessionId, userID, score);
//
//        mockMvc.perform(post("/api/sessions/scoreUpdate")
//                        .param("sessionId", sessionId)
//                        .param("userID", userID)
//                        .param("score", String.valueOf(score)))
//                .andExpect(status().isNotFound())
//                .andExpect(content().string("User not found"));
//    }
}

