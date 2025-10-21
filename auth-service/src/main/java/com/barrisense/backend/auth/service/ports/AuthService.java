package com.barrisense.backend.auth.service.ports;

import com.barrisense.backend.auth.dto.AuthDtos;
import com.barrisense.backend.auth.dto.TokenPair;
import org.springframework.stereotype.Service;

@Service
public interface AuthService {

    TokenPair register(AuthDtos.RegisterRequest req);

    TokenPair login(AuthDtos.LoginRequest req);

    String refresh(String refreshToken);

}

