package com.barrisense.backend.user.service;

import com.barrisense.backend.user.entity.User;
import com.barrisense.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public User getById(UUID id) {
        log.debug("Fetching user by id {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found with id " + id));
    }

    @Transactional(readOnly = true)
    public User getByUsername(String username) {
        log.debug("Fetching user by username {}", username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User not found with username " + username));
    }

    @Transactional
    public User create(User user) {
        log.debug("Creating user {}", user.getUsername());

        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalStateException("Username already exists: " + user.getUsername());
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalStateException("Email already exists: " + user.getEmail());
        }
        
        return userRepository.save(user);
    }

    @Transactional
    public User updateByUsername(String username, User incoming) {
        log.debug("Updating user {}", username);

        User stored = userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User not found with username " + username));

        // Only update mutable fields
        if (incoming.getEmail() != null && !incoming.getEmail().equals(stored.getEmail())) {
            if (userRepository.existsByEmail(incoming.getEmail())) {
                throw new IllegalStateException("Email already exists: " + incoming.getEmail());
            }
            stored.setEmail(incoming.getEmail());
        }

        if (incoming.getProfilePictureUrl() != null) {
            stored.setProfilePictureUrl(incoming.getProfilePictureUrl());
        }

        stored.setActive(incoming.isActive());

        return userRepository.save(stored);
    }

    @Transactional
    public void deleteByUsername(String username) {
        log.debug("Deleting user {}", username);
        if (!userRepository.existsByUsername(username)) {
            throw new NoSuchElementException("User not found with username " + username);
        }
        userRepository.deleteByUsername(username);
    }
}
