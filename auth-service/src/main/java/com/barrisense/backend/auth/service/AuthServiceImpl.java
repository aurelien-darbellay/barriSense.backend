package com.barrisense.backend.auth.service;

import com.barrisense.backend.auth.domain.Role;
import com.barrisense.backend.auth.domain.User;
import com.barrisense.backend.auth.dto.AuthDtos;
import com.barrisense.backend.auth.dto.TokenPair;
import com.barrisense.backend.auth.dto.UserCreatedEvent;
import com.barrisense.backend.auth.messaging.UserEventPublisher;
import com.barrisense.backend.auth.repository.UserRepository;
import com.barrisense.backend.auth.service.mappers.Mappers;
import com.barrisense.backend.auth.service.ports.AuthService;
import com.barrisense.backend.auth.service.ports.JwtService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtServiceImpl;
    private final UserDetailsService userDetailsService;
    private final UserEventPublisher userEventPublisher;
    private final Mappers mappers;

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    public TokenPair register(AuthDtos.RegisterRequest req) {
        log.debug("Register new user: {}", req);
        if (userRepository.existsByUsername(req.username())) {
            throw new IllegalStateException("Username already exists");
        }

        User user = User.builder()
                .username(req.username())
                .password(passwordEncoder.encode(req.password()))
                .roles(Set.of(Role.ROLE_USER))
                .build();

        UserDetails userDetails = mappers.mapUserToUserDetails(userRepository.save(user));
        UserCreatedEvent event = new UserCreatedEvent(user.getId(), user.getUsername(), user.getRoles());
        userEventPublisher.sendUserCreated(event);
        return createTokenPair(userDetails);
    }

    public TokenPair login(AuthDtos.LoginRequest req) {
        log.debug("Login in: {}", req);
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.username(), req.password())
            );
            UserDetails user = (UserDetails) auth.getPrincipal();
            return createTokenPair(user);
        } catch (BadCredentialsException e) {
            throw new IllegalArgumentException("Invalid credentials");
        }
    }

    public String refresh(String refreshToken) {
        log.debug("Refreshing Jwt: {}", refreshToken);
        String username = jwtServiceImpl.extractUsername(refreshToken);

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        if (!jwtServiceImpl.isTokenValid(refreshToken, userDetails)) {
            throw new IllegalArgumentException("Invalid or expired refresh token");
        }
        return jwtServiceImpl.generateAccessToken(userDetails);
    }

    private TokenPair createTokenPair(UserDetails userDetails) {
        String accessToken = jwtServiceImpl.generateAccessToken(userDetails);
        String refreshToken = jwtServiceImpl.generateRefreshToken(userDetails);

        return new TokenPair(accessToken, refreshToken);
    }
}

