package com.barrisense.backend.auth.controller;

import com.barrisense.backend.auth.service.AuthService;
import com.barrisense.backend.auth.util.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth/public")
@RequiredArgsConstructor
public class PublicAuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody AuthDtos.RegisterRequest req) {
        try {
            authService.register(req);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "User registered successfully"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody AuthDtos.LoginRequest req, HttpServletResponse response) {
        try {
            var tokens = authService.login(req);
            response.addHeader("Set-Cookie", CookieUtil.formatCookieHeader("JWT", tokens.accessToken(), 3600));
            response.addHeader("Set-Cookie", CookieUtil.formatCookieHeader("JWT_REFRESH", tokens.refreshToken(), 604800));
            return ResponseEntity.ok(Map.of("message", "Login successful"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refresh(@CookieValue(value = "JWT_REFRESH", required = false) String refreshToken,
                                                       HttpServletResponse response) {
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Missing refresh token"));
        }

        try {
            String newAccessToken = authService.refresh(refreshToken);
            response.addHeader("Set-Cookie", CookieUtil.formatCookieHeader("JWT", newAccessToken, 3600));
            return ResponseEntity.ok(Map.of("message", "Access token refreshed"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletResponse response) {
        response.addHeader("Set-Cookie", "JWT=; Path=/; Max-Age=0; HttpOnly; SameSite=Lax");
        response.addHeader("Set-Cookie", "JWT_REFRESH=; Path=/; Max-Age=0; HttpOnly; SameSite=Lax");
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}

