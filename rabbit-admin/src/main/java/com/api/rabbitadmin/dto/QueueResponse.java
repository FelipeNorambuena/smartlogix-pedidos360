package com.api.rabbitadmin.dto;

public record QueueResponse(String name, boolean durable, String deadLetterExchange) {
}
