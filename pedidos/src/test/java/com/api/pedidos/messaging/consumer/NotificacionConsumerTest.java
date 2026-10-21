package com.api.pedidos.messaging.consumer;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.api.pedidos.messaging.OrderCreatedMessage;
import com.rabbitmq.client.Channel;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/*
 * Pruebas del consumidor de notificaciones de pedido creado.
 * Verifican la confirmacion manual (ACK/NACK) exigida por la pauta: si el
 * procesamiento del mensaje falla, debe rechazarse sin reintentar en la
 * misma cola para que RabbitMQ lo reenrute a pedidos.notificaciones.dlq.
 */
@ExtendWith(MockitoExtension.class)
class NotificacionConsumerTest {

    @Mock
    private Channel channel;

    private final NotificacionConsumer consumer = new NotificacionConsumer();

    private final OrderCreatedMessage message = new OrderCreatedMessage(
            UUID.randomUUID(), UUID.randomUUID(), "Av. Siempre Viva 123", BigDecimal.valueOf(49990), OffsetDateTime.now());

    @Test
    void onOrderCreated_whenProcessingSucceeds_acknowledgesMessage() throws Exception {
        consumer.onOrderCreated(message, channel, 3L);

        verify(channel).basicAck(3L, false);
        verify(channel, never()).basicNack(anyLong(), anyBoolean(), anyBoolean());
    }

    @Test
    void onOrderCreated_whenAckFails_rejectsMessageWithoutRequeue() throws Exception {
        doThrow(new IOException("canal caido")).when(channel).basicAck(3L, false);

        consumer.onOrderCreated(message, channel, 3L);

        verify(channel).basicNack(3L, false, false);
    }
}
