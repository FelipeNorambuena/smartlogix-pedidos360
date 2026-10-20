package com.api.inventario.messaging.consumer;

import com.api.inventario.messaging.StockLowAlertMessage;
import com.rabbitmq.client.Channel;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/*
 * Consumidor del dominio inventario: registra (simulado) la alerta de stock
 * bajo para el equipo de reposición. ACK manual: confirma solo si el
 * registro tuvo éxito; si falla, NACK sin reintentar en la misma cola para
 * que RabbitMQ reenrute el mensaje a inventario.alertas.dlq.
 */
@Component
public class AlertaStockConsumer {

    private static final Logger log = LoggerFactory.getLogger(AlertaStockConsumer.class);

    @RabbitListener(queues = "${app.rabbitmq.queue.alertas}")
    public void onStockLow(
            @Payload StockLowAlertMessage message,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        try {
            log.warn(
                    "ALERTA DE STOCK BAJO - SKU {}: libre {} <= punto de reposicion {}",
                    message.sku(),
                    message.stockFree(),
                    message.reorderPoint());
            // Aquí iría la notificación real al equipo de reposición; por ahora se simula con el log anterior.
            channel.basicAck(deliveryTag, false);
        } catch (Exception exception) {
            log.error("No se pudo registrar la alerta de stock para {}", message.sku(), exception);
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
