package com.api.rabbitadmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/* Body para enlazar una cola a un exchange con una routing key (vacia para fanout). */
public record BindingCreateRequest(
        @NotBlank(message = "El nombre del exchange es obligatorio") @Size(max = 255) String exchange,
        @NotBlank(message = "El nombre de la cola es obligatorio") @Size(max = 255) String queue,
        @Size(max = 255) String routingKey) {

    public BindingCreateRequest {
        if (routingKey == null) {
            routingKey = "";
        }
    }
}
