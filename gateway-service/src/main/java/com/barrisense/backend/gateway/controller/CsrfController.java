package com.barrisense.backend.gateway.controller;

import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/csrf")
public class CsrfController {

    @GetMapping
    public ResponseEntity<Map<String, String>> getCsrfToken(ServerHttpResponse response) {
        String token = UUID.randomUUID().toString();

        ResponseCookie cookie = ResponseCookie.from("XSRF-TOKEN", token)
                .path("/")
                .sameSite("Lax")
                .httpOnly(false)
                .build();

        response.addCookie(cookie);

        return ResponseEntity.ok(Map.of("token", token));
    }
}

