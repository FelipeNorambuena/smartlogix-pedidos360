package com.api.inventario.messaging;

import java.time.OffsetDateTime;
import java.util.UUID;

/* Payload publicado en inventario.alertas.q cuando el stock libre cae al punto de reposición o por debajo. */
public record StockLowAlertMessage(
        UUID productId,
        String sku,
        int stockAvailable,
        int stockReserved,
        int stockFree,
        int reorderPoint,
        OffsetDateTime detectedAt) {
}
