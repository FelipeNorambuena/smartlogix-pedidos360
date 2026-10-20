package com.api.rabbitadmin.dto;

public record ExchangeResponse(String name, ExchangeType type, boolean durable) {
}
