package com.barrisense.backend.auth.service.ports;

import com.barrisense.backend.auth.domain.User;

import java.util.Optional;
import java.util.UUID;

/**
 * Domain-facing repository port for Users. Implementations adapt infrastructure (JPA, JDBC, etc.)
 * to this interface so the service layer remains framework-agnostic.
 */
public interface UserRepositoryPort {
    Optional<User> findById(UUID id);

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    User save(User user);
}
