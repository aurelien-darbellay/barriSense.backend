package com.barrisense.backend.auth.controller;

import com.barrisense.backend.auth.dto.AuthDtos;
import com.barrisense.backend.auth.service.AuthServiceImpl;
import com.barrisense.backend.auth.util.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class PublicAuthController {

    private final AuthServiceImpl authServiceImpl;

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody AuthDtos.RegisterRequest req) {
        log.debug("Register new user {}:", req);
        try {
            authServiceImpl.register(req);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "User registered successfully"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody AuthDtos.LoginRequest req, HttpServletResponse response) {
        log.debug("Login in for user {}:", req);
        try {
            var tokens = authServiceImpl.login(req);
            response.addHeader("Set-Cookie", CookieUtil.formatCookieHeader("JWT", tokens.accessToken(), 3600));
            response.addHeader("Set-Cookie", CookieUtil.formatCookieHeader("JWT_REFRESH", tokens.refreshToken(), 604800));
            return ResponseEntity.ok(Map.of("message", "Login successful"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", e.getMessage()));
        }
    }
}

