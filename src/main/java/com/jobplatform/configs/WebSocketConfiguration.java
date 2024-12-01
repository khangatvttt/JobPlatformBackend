package com.jobplatform.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer {
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Enable a simple message broker for the "/topic" endpoint
        registry.enableSimpleBroker("/topic");
        // Prefix for sending messages from client to server
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket endpoint that clients use to connect
        registry.addEndpoint("/chat").withSockJS();
        registry.addEndpoint("/chat").setAllowedOrigins("http://localhost:5173").withSockJS();
        registry.addEndpoint("/chat").setAllowedOrigins("http://localhost:5173");

        registry.addEndpoint("/chat");

    }
}
