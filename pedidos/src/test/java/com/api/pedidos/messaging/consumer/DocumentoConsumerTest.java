package com.api.pedidos.messaging.consumer;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.api.pedidos.messaging.OrderConfirmedMessage;
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
 * Pruebas del consumidor de generacion de documentos/tickets.
 * Verifican la confirmacion manual (ACK/NACK) exigida por la pauta: si el
 * procesamiento del mensaje falla, debe rechazarse sin reintentar en la
 * misma cola para que RabbitMQ lo reenrute a pedidos.documentos.dlq.
 */
@ExtendWith(MockitoExtension.class)
class DocumentoConsumerTest {

    @Mock
    private Channel channel;

    private final DocumentoConsumer consumer = new DocumentoConsumer();

    private final OrderConfirmedMessage message = new OrderConfirmedMessage(
            UUID.randomUUID(), UUID.randomUUID(), BigDecimal.valueOf(49990), OffsetDateTime.now());

    @Test
    void onOrderConfirmed_whenProcessingSucceeds_acknowledgesMessage() throws Exception {
        consumer.onOrderConfirmed(message, channel, 5L);

        verify(channel).basicAck(5L, false);
        verify(channel, never()).basicNack(anyLong(), anyBoolean(), anyBoolean());
    }

    @Test
    void onOrderConfirmed_whenAckFails_rejectsMessageWithoutRequeue() throws Exception {
        doThrow(new IOException("canal caido")).when(channel).basicAck(5L, false);

        consumer.onOrderConfirmed(message, channel, 5L);

        verify(channel).basicNack(5L, false, false);
    }
}
