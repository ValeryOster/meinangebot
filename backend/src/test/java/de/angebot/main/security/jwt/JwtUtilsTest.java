package de.angebot.main.security.jwt;

import de.angebot.main.security.services.UserDetailsImpl;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Prüft Erzeugen und Prüfen der JWTs mit JJWT 0.13. */
class JwtUtilsTest {

    /** 512-Bit-Testschlüssel (Base64), nur für Tests. */
    private static final String SECRET =
            "dGVzdC1zZWNyZXQtZm9yLXVuaXQtdGVzdHMtb25seS10ZXN0LXNlY3JldC1mb3ItdW5pdC10ZXN0cy1vbmx5LTEyMzQ1Njc4OTA=";

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", SECRET);
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 60_000);
    }

    @Test
    void generatedTokenIsValidAndContainsUsername() {
        UserDetailsImpl user = new UserDetailsImpl(1L, "admin", "admin@example.local", "x",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        String token = jwtUtils.generateJwtToken(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));

        assertThat(jwtUtils.validateJwtToken(token)).isTrue();
        assertThat(jwtUtils.getUserNameFromJwtToken(token)).isEqualTo("admin");
    }

    @Test
    void tokenWithOtherKeyIsRejected() {
        String foreignToken = Jwts.builder()
                .subject("admin")
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(
                        "b3RoZXItc2VjcmV0LW90aGVyLXNlY3JldC1vdGhlci1zZWNyZXQtb3RoZXItc2VjcmV0LW90aGVyLXNlY3JldC0xMjM0NTY3OA==")))
                .compact();

        assertThat(jwtUtils.validateJwtToken(foreignToken)).isFalse();
    }

    @Test
    void expiredTokenIsRejected() {
        String expired = Jwts.builder()
                .subject("admin")
                .issuedAt(new Date(System.currentTimeMillis() - 120_000))
                .expiration(new Date(System.currentTimeMillis() - 60_000))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET)))
                .compact();

        assertThat(jwtUtils.validateJwtToken(expired)).isFalse();
    }

    @Test
    void garbageIsRejected() {
        assertThat(jwtUtils.validateJwtToken("kein.jwt.token")).isFalse();
        assertThat(jwtUtils.validateJwtToken("")).isFalse();
    }
}
