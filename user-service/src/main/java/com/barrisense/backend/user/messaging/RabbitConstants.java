package com.barrisense.backend.user.messaging;

public class RabbitConstants {
    public static final String USER_EXCHANGE = "user.exchange";
    public static final String USER_CREATED_QUEUE = "user.created.queue";
    public static final String USER_CREATED_ROUTING_KEY = "user.created";

    private RabbitConstants() {
    }

}
