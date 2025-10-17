package com.barrisense.backend.gateway.config;

import com.barrisense.backend.gateway.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.security.web.server.csrf.ServerCsrfTokenRequestHandler;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {

        // ----- CSRF (double-submit cookie + header) -----
        http.csrf(csrf -> csrf
                .csrfTokenRepository(CookieServerCsrfTokenRepository.withHttpOnlyFalse())
                .requireCsrfProtectionMatcher(exchange -> mapBooleanToMonoMatchResult(requiresCsrfProtection(exchange)))
                .csrfTokenRequestHandler(doubleSubmitCsrfTokenHandler())
        );

        // ----- Authorization rules -----
        http.authorizeExchange(auth -> auth
                .matchers(publicPathMatcher()).permitAll()
                // Public and auth endpoints
                .pathMatchers("/csrf").permitAll()
                // Protected endpoints
                // Everything else
                .anyExchange().authenticated()
        );

        // ----- Custom JWT Authentication filter -----
        http.addFilterBefore(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION);

        // Disable login/logout defaults (we use JWT + CSRF)
        http.formLogin(ServerHttpSecurity.FormLoginSpec::disable);
        http.httpBasic(ServerHttpSecurity.HttpBasicSpec::disable);

        // Enable CORS (handled globally or via config)
        http.cors(cors -> cors.configurationSource(corsConfigurationSource));

        return http.build();
    }

    /**
     * Determine when CSRF protection is required.
     * Applies to POST, PUT, PATCH, DELETE under /protected/**,
     * excluding auth endpoints like /auth/login, /auth/register, /auth/logout.
     */
    private boolean requiresCsrfProtection(ServerWebExchange exchange) {
        String path = exchange.getRequest().getPath().toString();
        String method = exchange.getRequest().getMethod().name().toUpperCase();

        boolean isStateChanging = method.matches("POST|PUT|PATCH|DELETE");
        boolean isProtectedPath = path.contains("/protected/");
        boolean isAuthException =
                path.contains("/public/");
        return isStateChanging && isProtectedPath && !isAuthException;
    }

    private Mono<ServerWebExchangeMatcher.MatchResult> mapBooleanToMonoMatchResult(boolean result) {
        return result ? ServerWebExchangeMatcher.MatchResult.match() : ServerWebExchangeMatcher.MatchResult.notMatch();
    }

    private ServerWebExchangeMatcher publicPathMatcher() {
        return exchange -> {
            String path = exchange.getRequest().getPath().toString();
            boolean isPublic = path.contains("/public/");
            return isPublic ?
                    ServerWebExchangeMatcher.MatchResult.match() :
                    ServerWebExchangeMatcher.MatchResult.notMatch();
        };
    }

    @Bean
    public ServerCsrfTokenRequestHandler doubleSubmitCsrfTokenHandler() {
        return new ServerCsrfTokenRequestHandler() {

            @Override
            public void handle(ServerWebExchange exchange, Mono<CsrfToken> monoCsrf) {
                // Expose the CSRF token as an exchange attribute for controller access
                exchange.getAttributes().put(CsrfToken.class.getName(), monoCsrf);
            }

            @Override
            public Mono<String> resolveCsrfTokenValue(ServerWebExchange exchange, CsrfToken token) {
                // Read CSRF token from the X-XSRF-TOKEN header
                return Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst(token.getHeaderName()));
            }
        };
    }

}

