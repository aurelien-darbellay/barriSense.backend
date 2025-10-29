package com.barrisense.backend.user.controller;

import com.barrisense.backend.user.entity.User;
import com.barrisense.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/protected")
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public User getById(@PathVariable Long id) {
        log.debug("HTTP GET /{id} -> {}", id);
        return userService.getById(id);
    }

    @GetMapping("/by-username/{username}")
    public User getByUsername(@PathVariable String username) {
        log.debug("HTTP GET /by-username/{} ", username);
        return userService.getByUsername(username);
    }

    @PostMapping("/new")
    @ResponseStatus(HttpStatus.CREATED)
    public User create(@RequestBody User user) {
        log.debug("HTTP POST /new body={}", user);
        return userService.create(user);
    }

    @PutMapping("/by-username/{username}")
    public User updateByUsername(@PathVariable String username, @RequestBody User updatedUser) {
        log.debug("HTTP PUT /by-username/{} body={}", username, updatedUser);
        return userService.updateByUsername(username, updatedUser);
    }

    @DeleteMapping("/by-username/{username}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteByUsername(@PathVariable String username) {
        log.debug("HTTP DELETE /by-username/{}", username);
        userService.deleteByUsername(username);
    }
}
