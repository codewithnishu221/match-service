package match.service.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String jwtSecretKey;
    @Value("${app.jwt.expiration}")
    private Long jwtExpiration;

    private SecretKey getSecretKey(){
        return Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8));
    }


    public String extractEmail(String token){
        return extractAllClaims(token).getSubject();
    }

    public boolean validateToken(String token, String expectedEmail){
        String emailFromToken = extractEmail(token);
        return emailFromToken.equals(expectedEmail) && !isTokenExpired(token);
    }
    private boolean isTokenExpired(String token){
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    private Claims extractAllClaims(String token){
        return  Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long extractUserId(String token){
        Claims claims = extractAllClaims(token);
        return claims.get("userId", Long.class);
    }
}
