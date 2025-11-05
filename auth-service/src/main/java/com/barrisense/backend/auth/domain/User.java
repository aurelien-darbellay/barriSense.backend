package com.barrisense.backend.auth.domain;

import lombok.*;

import java.util.Set;
import java.util.UUID;

/**
 * Framework-agnostic domain model for User.
 * No JPA or validation annotations here — persistence is handled by adapters and entities.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    private UUID id;
    private String username;
    private String password;
    @Builder.Default
    private Set<Role> roles = Set.of(Role.ROLE_USER);
}
