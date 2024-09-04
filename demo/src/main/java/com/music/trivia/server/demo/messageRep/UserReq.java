package com.music.trivia.server.demo.messageRep;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UserReq {
    private String user;
    private String sessionID;

    // Constructor with JsonCreator and JsonProperty annotations
    @JsonCreator
    public UserReq(@JsonProperty("User") String user, @JsonProperty("SessionID") String sessionID) {
        this.user = user;
        this.sessionID = sessionID;
    }

    // Getters and setters
    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getSessionID() {
        return sessionID;
    }

    public void setSessionID(String sessionID) {
        this.sessionID = sessionID;
    }
}
