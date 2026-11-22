package de.angebot.main.security.jwt;

import de.angebot.main.security.services.UserDetailsImpl;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
@Slf4j
public class JwtUtils {

	@Value("${app.jwtSecret}")
	private String jwtSecret;

	@Value("${app.jwtExpirationMs}")
	private int jwtExpirationMs;

	private SecretKey key;

	/** Prüft den Schlüssel beim Start, damit eine fehlende/zu kurze Konfiguration sofort auffällt. */
	@PostConstruct
	void initKey() {
		if (jwtSecret == null || jwtSecret.isBlank()) {
			throw new IllegalStateException("app.jwtSecret fehlt – Umgebungsvariable APP_JWT_SECRET setzen.");
		}
		byte[] bytes = Decoders.BASE64.decode(jwtSecret.trim());
		if (bytes.length < 64) {
			throw new IllegalStateException("app.jwtSecret ist zu kurz (" + bytes.length
					+ " Byte, mindestens 64 Byte für HS512).");
		}
		key = Keys.hmacShaKeyFor(bytes);
	}

	private SecretKey signingKey() {
		if (key == null) {
			initKey();
		}
		return key;
	}

	public String generateJwtToken(Authentication authentication) {
		UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

		return Jwts.builder()
				.subject(userPrincipal.getUsername())
				.issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
				.signWith(signingKey())
				.compact();
	}

	public String getUserNameFromJwtToken(String token) {
		return Jwts.parser()
				.verifyWith(signingKey())
				.build()
				.parseSignedClaims(token)
				.getPayload()
				.getSubject();
	}

	public boolean validateJwtToken(String authToken) {
		try {
			Jwts.parser().verifyWith(signingKey()).build().parseSignedClaims(authToken);
			return true;
		} catch (SecurityException e) {
			log.error("Invalid JWT signature: {}", e.getMessage());
		} catch (MalformedJwtException e) {
			log.error("Invalid JWT token: {}", e.getMessage());
		} catch (ExpiredJwtException e) {
			log.error("JWT token is expired: {}", e.getMessage());
		} catch (UnsupportedJwtException e) {
			log.error("JWT token is unsupported: {}", e.getMessage());
		} catch (IllegalArgumentException e) {
			log.error("JWT claims string is empty: {}", e.getMessage());
		}

		return false;
	}
}
