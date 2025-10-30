package com.barrisense.backend.user.dto;

import com.barrisense.backend.user.entity.Role;

import java.util.Set;
import java.util.UUID;

public record UserCreatedEvent(UUID id, String username, Set<Role> roles) {
}