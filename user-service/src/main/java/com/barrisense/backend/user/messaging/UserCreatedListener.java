package com.barrisense.backend.user.messaging;

import com.barrisense.backend.user.dto.UserCreatedEvent;
import com.barrisense.backend.user.entity.User;
import com.barrisense.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static com.barrisense.backend.user.messaging.RabbitConstants.USER_CREATED_QUEUE;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserCreatedListener {

    private final UserService userService;

    @RabbitListener(queues = USER_CREATED_QUEUE)
    public void handleUserCreated(UserCreatedEvent event) {
        log.info("📥 Received new user: {} with ID {}",
                event.username(), event.id());
        userService.create(User.builder()
                .username(event.username())
                .id(event.id())
                .roles(event.roles())
                .build());
    }
}
