package com.music.trivia.server.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.context.annotation.Lazy;

@Configuration
@EnableWebSocketMessageBroker
public class WebConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthorizationHandler authorizationHandler;

    @Autowired
    public WebConfig(@Lazy WebSocketAuthorizationHandler authorizationHandler) {
        this.authorizationHandler = authorizationHandler;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/gs-guide-websocket").setAllowedOrigins("http://localhost:3000").withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(authorizationHandler);
    }
}