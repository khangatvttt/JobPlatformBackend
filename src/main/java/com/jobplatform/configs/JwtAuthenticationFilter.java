package com.jobplatform.configs;

import com.jobplatform.services.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final List<WhitelistEntry> whitelist = new ArrayList<>();


    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserDetailsService userDetailsService
    ) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;

        // Endpoint that no need filtered
        whitelist.add(new WhitelistEntry("/auth", null));          // Allow all methods for /auth
        whitelist.add(new WhitelistEntry("/jobs", "GET"));
        whitelist.add(new WhitelistEntry("/chat", null));          // Allow all methods for /auth

    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        //Not filter the /auth endpoint
        if (isWhitelisted(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new JwtException("Token is missing");
            }

            final String jwt = authHeader.substring(7);

            final String tokenType = jwtService.getTokenType(jwt);
            if (!Objects.equals(tokenType, JwtService.TokenType.ACCESS_TOKEN.toString())){
                throw new JwtException("Invalid token type");
            }

            final String userEmail = jwtService.extractUsername(jwt);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (userEmail != null && authentication == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception exception) {
            response.setStatus(401);
            response.setContentType("application/json");

            JSONObject json = new JSONObject();
            json.put("timestamp", LocalDateTime.now());
            json.put("status", "Unauthorized");
            json.put("error", exception.getMessage());
            response.getOutputStream().write(json.toString().getBytes());
            response.getOutputStream().flush();
            return;
        }
        filterChain.doFilter(request, response);

    }

    private boolean isWhitelisted(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String method = request.getMethod();

        return whitelist.stream()
                .anyMatch(entry -> entry.matches(uri, method));
    }


    private static class WhitelistEntry {
        private final String pathPrefix;
        private final String allowedMethod;

        public WhitelistEntry(String pathPrefix, String allowedMethod) {
            this.pathPrefix = pathPrefix;
            this.allowedMethod = allowedMethod;
        }

        public boolean matches(String uri, String method) {
            // Check if URI starts with the whitelist path and method matches (or is null for any method)
            return uri.startsWith(pathPrefix) && (allowedMethod == null || allowedMethod.equalsIgnoreCase(method));
        }
    }
}
