package com.api.pedidos.messaging;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/* Payload publicado en pedidos.notificaciones.q cuando se crea un pedido. */
public record OrderCreatedMessage(
        UUID orderId,
        UUID customerId,
        String shippingAddress,
        BigDecimal totalAmount,
        OffsetDateTime createdAt) {
}
