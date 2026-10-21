package com.api.inventario.messaging.consumer;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.api.inventario.messaging.StockLowAlertMessage;
import com.rabbitmq.client.Channel;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/*
 * Pruebas del consumidor de alertas de stock bajo.
 * Verifican la confirmacion manual (ACK/NACK) exigida por la pauta: si el
 * procesamiento del mensaje falla, debe rechazarse sin reintentar en la
 * misma cola para que RabbitMQ lo reenrute a inventario.alertas.dlq.
 */
@ExtendWith(MockitoExtension.class)
class AlertaStockConsumerTest {

    @Mock
    private Channel channel;

    private final AlertaStockConsumer consumer = new AlertaStockConsumer();

    private final StockLowAlertMessage message = new StockLowAlertMessage(
            UUID.randomUUID(), "SLX-ALI-003", 20, 3, 17, 18, OffsetDateTime.now());

    @Test
    void onStockLow_whenProcessingSucceeds_acknowledgesMessage() throws Exception {
        consumer.onStockLow(message, channel, 7L);

        verify(channel).basicAck(7L, false);
        verify(channel, never()).basicNack(anyLong(), anyBoolean(), anyBoolean());
    }

    @Test
    void onStockLow_whenAckFails_rejectsMessageWithoutRequeue() throws Exception {
        doThrow(new IOException("canal caido")).when(channel).basicAck(7L, false);

        consumer.onStockLow(message, channel, 7L);

        verify(channel).basicNack(7L, false, false);
    }
}
