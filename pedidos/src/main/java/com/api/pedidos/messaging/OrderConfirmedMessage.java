package com.api.pedidos.messaging;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/* Payload publicado en pedidos.documentos.q cuando un pedido queda confirmado. */
public record OrderConfirmedMessage(
        UUID orderId,
        UUID customerId,
        BigDecimal totalAmount,
        OffsetDateTime confirmedAt) {
}
