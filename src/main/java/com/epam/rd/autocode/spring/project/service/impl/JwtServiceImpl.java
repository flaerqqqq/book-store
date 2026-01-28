package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.security.IdentifiableUser;
import com.epam.rd.autocode.spring.project.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

@Service
public class JwtServiceImpl implements JwtService {

    private final long expiration;
    private final SecretKey key;

    public JwtServiceImpl(@Value("${jwt.expiration}") long expiration,
                          @Value("${jwt.secret}") String secret) {
        key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

    @Override
    public String generateAccessToken(UserDetails userDetails) {
        Date iat = new Date();
        Date exp = new Date(iat.getTime() + expiration);
        return Jwts.builder()
                .setClaims(buildClaims(userDetails))
                .setSubject(userDetails.getUsername())
                .setIssuedAt(iat)
                .setExpiration(exp)
                .signWith(key)
                .compact();
    }

    @Override
    public String extractSubject(String token) {
        return extractClaims(token).getSubject();
    }

    @Override
    public boolean isTokenValid(String token, UserDetails userDetails) {
        Claims claims = extractClaims(token);
        String emailFromToken = claims.getSubject();
        String idFromToken = claims.get("publicId", String.class);

        boolean idMatches = true;
        if (userDetails instanceof IdentifiableUser identifiable) {
            idMatches = Objects.equals(idFromToken, identifiable.getUniqueIdentifier());
        }

        return Objects.equals(emailFromToken, userDetails.getUsername()) && idMatches;
    }

    private Map<String, Object> buildClaims(UserDetails userDetails) {
        return Map.of(
                "roles", userDetails.getAuthorities()
        );
    }

    private Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(this.key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}