package com.barrisense.backend.auth.service.ports;

import com.barrisense.backend.auth.dto.AuthDtos;
import com.barrisense.backend.auth.dto.TokenPair;

public interface AuthService {

    TokenPair register(AuthDtos.RegisterRequest req);

    TokenPair login(AuthDtos.LoginRequest req);

    String refresh(String refreshToken);

}

