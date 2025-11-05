package com.barrisense.backend.auth.repository.adapter;

import com.barrisense.backend.auth.domain.User;
import com.barrisense.backend.auth.persistence.entity.UserEntity;
import com.barrisense.backend.auth.repository.UserRepository;
import com.barrisense.backend.auth.service.ports.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SpringDataUserRepositoryAdapter implements UserRepositoryPort {

    private final UserRepository userRepository;
    private final UserEntityMapper mapper;

    @Override
    public Optional<User> findById(UUID id) {
        return userRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username).map(mapper::toDomain);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public User save(User user) {
        UserEntity entity = mapper.toEntity(user);
        UserEntity saved = userRepository.save(entity);
        return mapper.toDomain(saved);
    }
}
