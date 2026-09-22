package edu.ies63.rcc2026_backend.security.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

  @Value("${app.security.jwt.secret}")
  private String secretKey;

  @Value("${app.security.jwt.expiration}")
  private long jwtExpiration;

  // Genera un token para el usuario
  public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
    extraClaims.put("extra", "Dato Extra"); // (Opcional, según prefieras)
    return Jwts.builder()
        .claims(extraClaims)
        .subject(userDetails.getUsername())
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
        .signWith(getSigningKey())
        .compact();
  }

  private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
  }
}