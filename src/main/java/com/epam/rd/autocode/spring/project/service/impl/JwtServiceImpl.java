package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.security.IdentifiableUser;
import com.epam.rd.autocode.spring.project.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
public class JwtServiceImpl implements JwtService {

    private final long expiration;
    private final SecretKey key;

    public JwtServiceImpl(@Value("${jwt.expiration}") long expiration,
                          @Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
        log.info("JWT Service initialized with expiration time: {} ms", expiration);
    }

    @Override
    public String generateAccessToken(UserDetails userDetails) {
        log.debug("Generating access token for user: {}", userDetails.getUsername());

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
        log.debug("Validating token for user: {}", userDetails.getUsername());

        Claims claims = extractClaims(token);
        String emailFromToken = claims.getSubject();
        String idFromToken = claims.get("publicId", String.class);

        boolean idMatches = true;
        if (userDetails instanceof IdentifiableUser identifiable) {
            idMatches = Objects.equals(idFromToken, identifiable.getUniqueIdentifier());
        }

        boolean isValid = Objects.equals(emailFromToken, userDetails.getUsername()) && idMatches;

        if (!isValid) {
            log.warn("Token validation failed for user: {}", userDetails.getUsername());
        }

        return isValid;
    }

    private Map<String, Object> buildClaims(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        if (userDetails instanceof IdentifiableUser identifiable) {
            claims.put("publicId", identifiable.getUniqueIdentifier());
        }

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        claims.put("roles", roles);

        return claims;
    }

    private Claims extractClaims(String token) {
        log.trace("Extracting claims from JWT token");
        return Jwts.parserBuilder()
                .setSigningKey(this.key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}