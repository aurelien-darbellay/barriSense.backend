package com.barrisense.backend.auth.dto;

public record TokenPair(String accessToken, String refreshToken) {
}
