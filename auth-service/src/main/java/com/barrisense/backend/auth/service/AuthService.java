package com.barrisense.backend.auth.service;

import com.barrisense.backend.auth.controller.AuthDtos;
import com.barrisense.backend.auth.domain.Role;
import com.barrisense.backend.auth.domain.User;
import com.barrisense.backend.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    static private final Logger log = LoggerFactory.getLogger(AuthService.class);

    public void register(AuthDtos.RegisterRequest req) {
        log.debug("Register new user: {}", req);
        if (userRepository.existsByUsername(req.username())) {
            throw new IllegalStateException("Username already exists");
        }

        User user = User.builder()
                .username(req.username())
                .password(passwordEncoder.encode(req.password()))
                .roles(Set.of(Role.ROLE_USER))
                .build();

        userRepository.save(user);
    }

    public TokenPair login(AuthDtos.LoginRequest req) {
        log.debug("Login in: {}", req);
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.username(), req.password())
            );

            UserDetails user = (UserDetails) auth.getPrincipal();

            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            return new TokenPair(accessToken, refreshToken);

        } catch (BadCredentialsException e) {
            throw new IllegalArgumentException("Invalid credentials");
        }
    }

    public String refresh(String refreshToken) {
        log.debug("Refreshing Jwt: {}", refreshToken);
        String username = jwtService.extractUsername(refreshToken);

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (!jwtService.isTokenValid(refreshToken, userDetails)) {
            throw new IllegalArgumentException("Invalid or expired refresh token");
        }

        return jwtService.generateAccessToken(userDetails);
    }

    // Inner record to return token pair
    public record TokenPair(String accessToken, String refreshToken) {
    }
}

