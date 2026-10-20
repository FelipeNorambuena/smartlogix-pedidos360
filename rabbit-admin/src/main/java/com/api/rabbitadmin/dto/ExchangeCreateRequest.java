package com.api.rabbitadmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/* Body para crear un exchange de cualquier tipo soportado por RabbitMQ. */
public record ExchangeCreateRequest(
        @NotBlank(message = "El nombre del exchange no puede estar vacio")
        @Size(max = 255)
        @Pattern(regexp = "^[a-zA-Z0-9_.\\-]+$", message = "El nombre solo puede contener letras, numeros, '.', '_' y '-'")
        String name,
        @NotNull(message = "El tipo de exchange es obligatorio")
        ExchangeType type,
        boolean durable) {
}
