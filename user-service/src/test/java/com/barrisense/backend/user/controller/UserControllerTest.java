package com.barrisense.backend.user.controller;

import com.barrisense.backend.user.entity.User;
import com.barrisense.backend.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private static User sampleUser() {
        return User.builder()
                .id(UUID.randomUUID())
                .username("johndoe")
                .email("john@example.com")
                .profilePictureUrl("https://example.com/pic.jpg")
                .createdAt(LocalDateTime.now())
                .active(true)
                .build();
    }

    @Test
    @DisplayName("GET /api/users/protected/{id} → returns user by id")
    void getById_ReturnsUser() throws Exception {
        User user = sampleUser();
        when(userService.getById(user.getId())).thenReturn(user);

        mockMvc.perform(get("/api/users/protected/{id}", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("johndoe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));

        Mockito.verify(userService).getById(user.getId());
    }

    @Test
    @DisplayName("GET /api/users/protected/by-username/{username} → returns user by username")
    void getByUsername_ReturnsUser() throws Exception {
        User user = sampleUser();
        when(userService.getByUsername("johndoe")).thenReturn(user);

        mockMvc.perform(get("/api/users/protected/by-username/{username}", "johndoe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.active").value(true));

        Mockito.verify(userService).getByUsername("johndoe");
    }

    @Test
    @DisplayName("POST /api/users/protected/new → creates new user")
    void create_CreatesNewUser() throws Exception {
        User requestUser = User.builder()
                .username("newuser")
                .email("new@example.com")
                .profilePictureUrl("https://example.com/pic.jpg")
                .active(true)
                .build();

        User savedUser = User.builder()
                .id(UUID.randomUUID())
                .username("newuser")
                .email("new@example.com")
                .profilePictureUrl("https://example.com/pic.jpg")
                .createdAt(LocalDateTime.now())
                .active(true)
                .build();

        when(userService.create(any(User.class))).thenReturn(savedUser);

        mockMvc.perform(post("/api/users/protected/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.email").value("new@example.com"));

        Mockito.verify(userService).create(any(User.class));
    }

    @Test
    @DisplayName("PUT /api/users/protected/by-username/{username} → updates existing user")
    void updateByUsername_UpdatesUser() throws Exception {
        User updateRequest = User.builder()
                .email("updated@example.com")
                .profilePictureUrl("https://example.com/updated.jpg")
                .active(false)
                .build();

        User updatedUser = sampleUser();
        updatedUser.setEmail("updated@example.com");
        updatedUser.setProfilePictureUrl("https://example.com/updated.jpg");
        updatedUser.setActive(false);

        when(userService.updateByUsername(eq("johndoe"), any(User.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/users/protected/by-username/{username}", "johndoe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("updated@example.com"))
                .andExpect(jsonPath("$.profilePictureUrl").value("https://example.com/updated.jpg"))
                .andExpect(jsonPath("$.active").value(false));

        Mockito.verify(userService).updateByUsername(eq("johndoe"), any(User.class));
    }

    @Test
    @DisplayName("DELETE /api/users/protected/by-username/{username} → deletes user by username")
    void deleteByUsername_DeletesUser() throws Exception {
        doNothing().when(userService).deleteByUsername("johndoe");

        mockMvc.perform(delete("/api/users/protected/by-username/{username}", "johndoe"))
                .andExpect(status().isNoContent());

        Mockito.verify(userService).deleteByUsername("johndoe");
    }
}
