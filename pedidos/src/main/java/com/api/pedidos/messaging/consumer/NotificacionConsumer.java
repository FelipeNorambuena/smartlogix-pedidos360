package com.api.pedidos.messaging.consumer;

import com.api.pedidos.messaging.OrderCreatedMessage;
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
 * Consumidor del dominio pedidos: envía (simulado) la notificación al
 * cliente cuando se crea un pedido. ACK manual: confirma solo si el envío
 * tuvo éxito; si falla, NACK sin reintentar en la misma cola para que
 * RabbitMQ reenrute el mensaje a pedidos.notificaciones.dlq.
 */
@Component
public class NotificacionConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificacionConsumer.class);

    @RabbitListener(queues = "${app.rabbitmq.queue.notificaciones}")
    public void onOrderCreated(
            @Payload OrderCreatedMessage message,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        try {
            log.info(
                    "Enviando notificación al cliente {} por el pedido {} (total: {})",
                    message.customerId(),
                    message.orderId(),
                    message.totalAmount());
            // Aquí iría la integración real (email/push); por ahora se simula con el log anterior.
            channel.basicAck(deliveryTag, false);
        } catch (Exception exception) {
            log.error("No se pudo procesar la notificación del pedido {}", message.orderId(), exception);
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
