package com.music.trivia.server.demo.model;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

@Component
public class SessionStorage {

    private static final Duration SESSION_TIMEOUT = Duration.ofHours(3);
    private HashMap<String, Session> storageHashMap = new HashMap<>();

    public String createSession(String password) {
        String sessionID = generateSessionID();
        Session session = new Session(sessionID, password);
        storageHashMap.put(sessionID, session);
        return sessionID;
    }

    public Session getSession(String sessionID) {
        cleanupExpiredSessions();
        Session session = storageHashMap.get(sessionID);
        if (session == null) {
            throw new IllegalArgumentException("Session not found");
        }
        return session;
    }

    public void removeSession(String sessionID) {
        storageHashMap.remove(sessionID);
    }

    public void storeSession(String sessionID, Session session) {
        storageHashMap.put(sessionID, session);
    }

    private String generateSessionID() {
        return UUID.randomUUID().toString();
    }

    @Scheduled(fixedRate = 3600000)  // Every hour
    private void cleanupExpiredSessions() {
        Instant now = Instant.now();
        Iterator<Session> iterator = storageHashMap.values().iterator();

        while (iterator.hasNext()) {
            Session session = iterator.next();
            if (Duration.between(session.getCreationTime(), now).compareTo(SESSION_TIMEOUT) > 0) {
                iterator.remove();
            }
        }
    }
}