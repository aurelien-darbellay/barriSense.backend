package com.barrisense.backend.auth.service;

import com.barrisense.backend.auth.domain.Role;
import com.barrisense.backend.auth.domain.User;
import com.barrisense.backend.auth.dto.AuthDtos;
import com.barrisense.backend.auth.dto.TokenPair;
import com.barrisense.backend.auth.messaging.UserEventPublisher;
import com.barrisense.backend.auth.repository.UserRepository;
import com.barrisense.backend.auth.service.mappers.Mappers;
import com.barrisense.backend.auth.service.ports.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;
    @Mock
    private UserEventPublisher publisher;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // 🧠 Real mapper (you can replace with your actual implementation if it’s a record/class)
        Mappers mappers = new Mappers();

        // ✅ Real CustomUserDetailsService (wired with mocks and real mapper)
        CustomUserDetailsService realUserDetailsService = new CustomUserDetailsService(userRepository, mappers);

        ReflectionTestUtils.setField(authService, "userDetailsService", realUserDetailsService);
        ReflectionTestUtils.setField(authService, "mappers", mappers);
    }

    @Test
    void register_ShouldCreateUserAndReturnTokens() {
        AuthDtos.RegisterRequest req = new AuthDtos.RegisterRequest("john", "password");

        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encoded");

        // simulate repository save
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        when(jwtService.generateAccessToken(any(UserDetails.class))).thenReturn("access");
        when(jwtService.generateRefreshToken(any(UserDetails.class))).thenReturn("refresh");

        TokenPair result = authService.register(req);

        assertEquals("access", result.accessToken());
        assertEquals("refresh", result.refreshToken());

        // verify correct user was saved
        User saved = captor.getValue();
        assertEquals("john", saved.getUsername());
        assertEquals(Set.of(Role.ROLE_USER), saved.getRoles());
        verify(passwordEncoder).encode("password");
        verify(userRepository).save(any());
    }

    @Test
    void register_ShouldThrow_WhenUsernameAlreadyExists() {
        when(userRepository.existsByUsername("john")).thenReturn(true);
        AuthDtos.RegisterRequest req = new AuthDtos.RegisterRequest("john", "pass");

        assertThrows(IllegalStateException.class, () -> authService.register(req));
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_ShouldReturnTokens_WhenCredentialsValid() {
        AuthDtos.LoginRequest req = new AuthDtos.LoginRequest("john", "pass");
        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("john").password("encoded").roles("USER").build();

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtService.generateAccessToken(userDetails)).thenReturn("access");
        when(jwtService.generateRefreshToken(userDetails)).thenReturn("refresh");

        TokenPair result = authService.login(req);

        assertEquals("access", result.accessToken());
        assertEquals("refresh", result.refreshToken());
        verify(authenticationManager).authenticate(any());
    }

    @Test
    void login_ShouldThrow_WhenBadCredentials() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad creds"));

        AuthDtos.LoginRequest req = new AuthDtos.LoginRequest("john", "wrong");

        assertThrows(IllegalArgumentException.class, () -> authService.login(req));
    }

    @Test
    void refresh_ShouldReturnNewAccessToken_WhenValid() {
        String refreshToken = "refresh123";
        String username = "john";
        User user = User.builder().username(username).password("encoded").roles(Set.of(Role.ROLE_USER)).build();

        when(jwtService.extractUsername(refreshToken)).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(jwtService.isTokenValid(eq(refreshToken), any(UserDetails.class))).thenReturn(true);
        when(jwtService.generateAccessToken(any(UserDetails.class))).thenReturn("newAccess");

        String result = authService.refresh(refreshToken);

        assertEquals("newAccess", result);
        verify(userRepository).findByUsername(username);
    }

    @Test
    void refresh_ShouldThrow_WhenTokenInvalid() {
        String refreshToken = "refresh123";
        String username = "john";
        User user = User.builder().username(username).password("encoded").roles(Set.of(Role.ROLE_USER)).build();

        when(jwtService.extractUsername(refreshToken)).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(jwtService.isTokenValid(eq(refreshToken), any(UserDetails.class))).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> authService.refresh(refreshToken));
        verify(userRepository).findByUsername(username);
    }
}
