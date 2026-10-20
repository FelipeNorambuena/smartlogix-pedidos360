package com.api.rabbitadmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/* Body para crear una cola. deadLetterExchange es opcional: si se indica, la cola queda enlazada a esa DLX. */
public record QueueCreateRequest(
        @NotBlank(message = "El nombre de la cola no puede estar vacio")
        @Size(max = 255)
        @Pattern(regexp = "^[a-zA-Z0-9_.\\-]+$", message = "El nombre solo puede contener letras, numeros, '.', '_' y '-'")
        String name,
        boolean durable,
        String deadLetterExchange) {

    public QueueCreateRequest {
        if (deadLetterExchange != null && deadLetterExchange.isBlank()) {
            deadLetterExchange = null;
        }
    }
}
