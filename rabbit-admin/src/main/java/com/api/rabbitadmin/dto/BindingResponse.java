package com.api.rabbitadmin.dto;

public record BindingResponse(String exchange, String queue, String routingKey) {
}
