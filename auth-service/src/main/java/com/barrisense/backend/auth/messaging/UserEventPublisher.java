package com.barrisense.backend.auth.messaging;

import com.barrisense.backend.auth.dto.UserCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;

import static com.barrisense.backend.auth.messaging.RabbitConstants.USER_CREATED_ROUTING_KEY;
import static com.barrisense.backend.auth.messaging.RabbitConstants.USER_EXCHANGE;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserEventPublisher {

    private final AmqpTemplate amqpTemplate;

    public void sendUserCreated(UserCreatedEvent event) {
        try {
            amqpTemplate.convertAndSend(USER_EXCHANGE, USER_CREATED_ROUTING_KEY, event);
            log.info("📤 Sent event to exchange={} using routingKey={}", USER_EXCHANGE, USER_CREATED_ROUTING_KEY);
        } catch (Exception e) {
            log.info("⚠️ Failed to send user.created event: {}", e.getMessage());
        }
    }
}
