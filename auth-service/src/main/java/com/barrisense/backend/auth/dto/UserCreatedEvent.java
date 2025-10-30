package com.barrisense.backend.auth.dto;

import com.barrisense.backend.auth.domain.Role;

import java.util.Set;
import java.util.UUID;

public record UserCreatedEvent(UUID id, String username, Set<Role> roles) {
}

