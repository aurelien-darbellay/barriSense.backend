package com.barrisense.backend.auth.repository.adapter;

import com.barrisense.backend.auth.domain.User;
import com.barrisense.backend.auth.persistence.entity.UserEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserEntityMapper {

    public User toDomain(UserEntity e) {
        if (e == null) return null;
        return User.builder()
                .id(e.getId())
                .username(e.getUsername())
                .password(e.getPassword())
                .roles(e.getRoles())
                .build();
    }

    public UserEntity toEntity(User u) {
        if (u == null) return null;
        return UserEntity.builder()
                .id(u.getId())
                .username(u.getUsername())
                .password(u.getPassword())
                .roles(u.getRoles())
                .build();
    }
}
