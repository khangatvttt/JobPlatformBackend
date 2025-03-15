package com.jobplatform.configs;

import com.jobplatform.services.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

public class AuthenticatedHandshakeHandler extends DefaultHandshakeHandler {

    private final JwtService jwtService;

    public AuthenticatedHandshakeHandler(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest httpRequest = servletRequest.getServletRequest();
            String token = httpRequest.getParameter("token");

            try {
                if (token != null && jwtService.isTokenValid(token)) {
                    String username = jwtService.extractUsername(token);
                    return new WebSocketUserPrincipal(username);
                }
            } catch (Exception ex) {
                return null;
            }
        }
        return null;
    }

    private static class WebSocketUserPrincipal implements Principal {
        private final String name;

        public WebSocketUserPrincipal(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}

