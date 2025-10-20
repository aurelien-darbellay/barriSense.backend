package com.barrisense.backend.auth.service;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceImplTest {

    private JwtServiceImpl jwtService;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() throws Exception {
        jwtService = new JwtServiceImpl();

        // inject values via reflection (since @Value not used in tests)
        setField(jwtService, "secret", "my-very-secret-test-key-12345678901234567890");
        setField(jwtService, "expirationSeconds", 2L); // short for test
        setField(jwtService, "refreshExpirationSeconds", 60L);

        userDetails = new User("alice", "password", Collections.emptyList());
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void generateAccessToken_containsUsernameAndIsValid() {
        String token = jwtService.generateAccessToken(userDetails);

        String extracted = jwtService.extractUsername(token);

        assertThat(extracted).isEqualTo("alice");
        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
        assertThat(jwtService.isTokenExpired(token)).isFalse();
    }

    @Test
    void generateRefreshToken_hasLongerExpiration() {
        String access = jwtService.generateAccessToken(userDetails);
        String refresh = jwtService.generateRefreshToken(userDetails);

        Date accessExp = jwtService.extractExpiration(access);
        Date refreshExp = jwtService.extractExpiration(refresh);

        assertThat(refreshExp).isAfter(accessExp);
    }

    @Test
    void tokenShouldBeInvalidForDifferentUser() {
        String token = jwtService.generateAccessToken(userDetails);
        UserDetails other = new User("bob", "password", Collections.emptyList());

        assertThat(jwtService.isTokenValid(token, other)).isFalse();
    }

    @Test
    void extractClaim_returnsExpectedValue() {
        String token = jwtService.generateAccessToken(userDetails);

        String subject = jwtService.extractClaim(token, Claims::getSubject);

        assertThat(subject).isEqualTo("alice");
    }

    @Test
    void expiredTokenDetectedProperly() throws InterruptedException {
        String token = jwtService.generateAccessToken(userDetails);
        Thread.sleep(4000); // wait > expirationSeconds (2s)
        assertThatThrownBy(() -> jwtService.extractUsername(token))
                .isInstanceOf(io.jsonwebtoken.ExpiredJwtException.class);
    }

    @Test
    void invalidToken_throwsExceptionWhenParsing() {
        String invalidToken = "not.a.valid.token";

        assertThatThrownBy(() -> jwtService.extractUsername(invalidToken))
                .isInstanceOf(Exception.class);
    }
}
