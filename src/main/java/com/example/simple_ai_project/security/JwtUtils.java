package com.example.simple_ai_project.security;

import com.example.simple_ai_project.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtils {

	@Value("${prox.app.jwtSecret}")
	private String jwtSecret;

	@Value("${prox.app.jwtExpirationMs}")
	private String jwtExpirationMs;

	public String generateToken(String username) {
		Instant expiration = Instant.now().plusMillis(Long.parseLong(jwtExpirationMs));
		return Jwts.builder()
				.subject(username)
				.issuedAt(Date.from(Instant.now()))
				.expiration(Date.from(expiration))
				.signWith(getSignInKey())
				.compact();
	}

	public String extractUsername(String token) {
		return getClaims(token).getSubject();
	}

	public boolean validateToken(String token) {
		try {
			Claims claims = getClaims(token);
			return !claims.getExpiration().before(new Date());
		} catch (Exception e) {
			return false;
		}
	}

	private Claims getClaims(String token) {
		return Jwts.parser()
				.verifyWith(getSignInKey())
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}

	private SecretKey getSignInKey() {
		byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
		return Keys.hmacShaKeyFor(keyBytes);
	}
}
