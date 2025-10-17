package com.barrisense.backend.auth.controller;

import com.barrisense.backend.auth.domain.Role;
import com.barrisense.backend.auth.domain.User;
import com.barrisense.backend.auth.repository.UserRepository;
import com.barrisense.backend.auth.security.JwtAuthenticationFilter;
import com.barrisense.backend.auth.security.JwtService;
import com.barrisense.backend.auth.util.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody AuthDtos.RegisterRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Username already exists"));
        }
        User u = User.builder()
                .username(req.username())
                .password(passwordEncoder.encode(req.password()))
                .roles(Set.of(Role.ROLE_USER))
                .build();
        userRepository.save(u);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "User registered"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthDtos.LoginRequest req, HttpServletResponse response) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.username(), req.password())
            );
            UserDetails user = (UserDetails) auth.getPrincipal();
            String access = jwtService.generateAccessToken(user);
            String refresh = jwtService.generateRefreshToken(user);

            // Create HttpOnly cookies
            Cookie accessCookie = CookieUtil.createHttpOnlyCookie(JwtAuthenticationFilter.ACCESS_COOKIE, access, (int) 3600, "/", false, "Lax");
            Cookie refreshCookie = CookieUtil.createHttpOnlyCookie("JWT_REFRESH", refresh, (int) 604800, "/", false, "Lax");

            // Add SameSite via header (Servlet Cookie API doesn't support it directly)
            response.addHeader("Set-Cookie", accessCookie.getName() + "=" + accessCookie.getValue() + "; Path=/; Max-Age=" + accessCookie.getMaxAge() + "; HttpOnly; SameSite=Lax");
            response.addHeader("Set-Cookie", refreshCookie.getName() + "=" + refreshCookie.getValue() + "; Path=/; Max-Age=" + refreshCookie.getMaxAge() + "; HttpOnly; SameSite=Lax");

            return ResponseEntity.ok(Map.of("message", "Logged in"));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid credentials"));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@CookieValue(value = "JWT_REFRESH", required = false) String refreshToken,
                                     HttpServletResponse response,
                                     @AuthenticationPrincipal UserDetails currentUser) {
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Missing refresh token"));
        }
        try {
            String username = jwtService.extractUsername(refreshToken);

            if (currentUser == null || !username.equals(currentUser.getUsername())) {
                // In stateless setup, we can load user by username, but here we just issue a new access token
                UserDetails user = org.springframework.security.core.userdetails.User
                        .withUsername(username)
                        .password("N/A")
                        .authorities("ROLE_USER")
                        .build();

                String newAccess = jwtService.generateAccessToken(user);
                Cookie accessCookie = CookieUtil.createHttpOnlyCookie(
                        JwtAuthenticationFilter.ACCESS_COOKIE,
                        newAccess,
                        3600,
                        "/",
                        false,
                        "Lax"
                );
                response.addHeader("Set-Cookie",
                        accessCookie.getName() + "=" + accessCookie.getValue() +
                                "; Path=/; Max-Age=" + accessCookie.getMaxAge() +
                                "; HttpOnly; SameSite=Lax");

                return ResponseEntity.ok(Map.of("message", "Access token refreshed"));


        }
            return ResponseEntity.ok(Map.of("message", "Access token still valid"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid refresh token"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal UserDetails user) {
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Unauthorized"));
        return ResponseEntity.ok(new AuthDtos.MeResponse(user.getUsername()));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        // Overwrite cookies with Max-Age=0
        response.addHeader("Set-Cookie", JwtAuthenticationFilter.ACCESS_COOKIE + "=; Path=/; Max-Age=0; HttpOnly; SameSite=Lax");
        response.addHeader("Set-Cookie", "JWT_REFRESH=; Path=/; Max-Age=0; HttpOnly; SameSite=Lax");
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }

    @GetMapping("/csrf")
    public Map<String, String> getCsrfToken(CsrfToken token) {
        // ✅ Get the existing CSRF token created by CookieCsrfTokenRepository
        return Map.of("token", token.getToken());
    }
}
