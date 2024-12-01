package com.jobplatform.services;

import com.jobplatform.models.UserAccount;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    public enum TokenType{
        ACCESS_TOKEN,
        REFRESH_TOKEN,
        VERIFICATION_TOKEN,
    }

    @Value("${security.jwt.access.secret-key}")
    private String secretKey;

    @Value("${security.jwt.access.expiration-time}")
    private long jwtExpiration;

    @Value("${security.jwt.refresh.expiration-time}")
    private long jwtRefreshExpiration;

    public String extractUsername(String token) {
        return extractClaim(token).getSubject();
    }

    public String getTokenType(String token){
        return (String) extractClaim(token).get("token_type");
    }

    public Claims extractClaim(String token) {
        return extractAllClaims(token);
    }

    public String generateToken(JwtService.TokenType tokenType, UserAccount user) {
        return  Jwts
                .builder()
                .claim("token_type",tokenType)
                .claim("role", user.getRole())
                .claim("user_id", user.getId())
                .subject(user.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey())
                .compact();

    }

    public String generateRefreshToken(String oldToken, UserDetails userDetails) {
        Date expiration = extractExpiration(oldToken);
        return  Jwts
                .builder()
                .claim("token_type",TokenType.REFRESH_TOKEN)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(expiration)
                .signWith(getSignInKey())
                .compact();

    }

    public String generateRefreshToken(UserDetails userDetails) {
        return  Jwts
                .builder()
                .claim("token_type",TokenType.REFRESH_TOKEN)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtRefreshExpiration))
                .signWith(getSignInKey())
                .compact();

    }

    public long getExpirationTime() {
        return jwtExpiration;
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    public boolean isTokenValid(String token) {
        final String username = extractUsername(token);
        return !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token).getExpiration();
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
