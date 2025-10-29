package com.barrisense.backend.auth.controller;

import com.barrisense.backend.auth.service.AuthServiceImpl;
import com.barrisense.backend.auth.util.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/protected")
@RequiredArgsConstructor
public class ProtectedAuthController {

    private final AuthServiceImpl authServiceImpl;
    static private final Logger log = LoggerFactory.getLogger(ProtectedAuthController.class);

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refresh(@CookieValue(value = "JWT_REFRESH", required = false) String refreshToken,
                                                       HttpServletResponse response) {
        log.debug("Refreshing JWT token with refresh token {}", refreshToken);

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Missing refresh token"));
        }
        try {
            String newAccessToken = authServiceImpl.refresh(refreshToken);
            response.addHeader("Set-Cookie", CookieUtil.formatCookieHeader("JWT", newAccessToken, 3600));
            return ResponseEntity.ok(Map.of("message", "Access token refreshed"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletResponse response) {
        log.debug("Logging out");

        response.addHeader("Set-Cookie", "JWT=; Path=/; Max-Age=0; HttpOnly; SameSite=Lax");
        response.addHeader("Set-Cookie", "JWT_REFRESH=; Path=/; Max-Age=0; HttpOnly; SameSite=Lax");
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}
