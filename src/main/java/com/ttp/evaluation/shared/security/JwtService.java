package com.ttp.evaluation.shared.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Сервис для работы с JWT токенами
 * Использует JJWT 0.12.x API
 */
@Service
public class JwtService {

    @Value("${app.security.jwt.secret-key}")
    private String secretKey;

    @Value("${app.security.jwt.expiration:86400000}") // 24 hours
    private long jwtExpiration;

    /**
     * Извлечь username (email) из токена
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Извлечь конкретный claim из токена
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Сгенерировать токен для пользователя
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Сгенерировать токен с дополнительными claims
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    /**
     * Построить JWT токен
     */
    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {
        return Jwts.builder()
                .claims(extraClaims)                    // ✅ claims() вместо setClaims()
                .subject(userDetails.getUsername())      // ✅ subject() вместо setSubject()
                .issuedAt(new Date(System.currentTimeMillis()))  // ✅ issuedAt() вместо setIssuedAt()
                .expiration(new Date(System.currentTimeMillis() + expiration)) // ✅ expiration() вместо setExpiration()
                .signWith(getSignInKey())                // ✅ signWith(Key) - новый API
                .compact();
    }

    /**
     * Проверить валидность токена
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Проверить не истек ли токен
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Извлечь дату истечения токена
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Извлечь все claims из токена
     * ✅ НОВЫЙ API для JJWT 0.12.x
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()                  // ✅ parser() - правильно для 0.12.x
                .verifyWith(getSignInKey())    // ✅ verifyWith() вместо setSigningKey()
                .build()                       // ✅ build() обязателен
                .parseSignedClaims(token)      // ✅ parseSignedClaims() вместо parseClaimsJws()
                .getPayload();                 // ✅ getPayload() вместо getBody()
    }

    /**
     * Получить ключ подписи
     */
    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}