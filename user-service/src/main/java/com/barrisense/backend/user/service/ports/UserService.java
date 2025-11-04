package com.barrisense.backend.user.service.ports;

import com.barrisense.backend.user.entity.User;

import java.util.UUID;

public interface UserService {

    User getById(UUID id);

    User getByUsername(String username);

    User create(User user);

    User updateByUsername(String username, User incoming);

    void deleteByUsername(String username);
}
