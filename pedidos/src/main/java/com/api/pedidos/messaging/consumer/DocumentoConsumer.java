package com.api.pedidos.messaging.consumer;

import com.api.pedidos.messaging.OrderConfirmedMessage;
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
 * Consumidor del dominio pedidos: genera (simulado) el ticket/boleta cuando
 * un pedido queda confirmado. ACK manual: confirma solo si la generación
 * tuvo éxito; si falla, NACK sin reintentar en la misma cola para que
 * RabbitMQ reenrute el mensaje a pedidos.documentos.dlq.
 */
@Component
public class DocumentoConsumer {

    private static final Logger log = LoggerFactory.getLogger(DocumentoConsumer.class);

    @RabbitListener(queues = "${app.rabbitmq.queue.documentos}")
    public void onOrderConfirmed(
            @Payload OrderConfirmedMessage message,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        try {
            log.info(
                    "Generando documento/ticket del pedido {} para el cliente {} (total: {})",
                    message.orderId(),
                    message.customerId(),
                    message.totalAmount());
            // Aquí iría la generación real del PDF/ticket; por ahora se simula con el log anterior.
            channel.basicAck(deliveryTag, false);
        } catch (Exception exception) {
            log.error("No se pudo generar el documento del pedido {}", message.orderId(), exception);
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
