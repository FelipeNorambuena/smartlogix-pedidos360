package com.api.pedidos.messaging;

import com.api.pedidos.config.RabbitMQProperties;
import com.api.pedidos.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/*
 * Único punto del dominio pedidos que publica eventos a RabbitMQ. Mantiene el
 * RabbitTemplate y los nombres de exchange/routing key fuera de OrderService,
 * que solo conoce "publica que se creó/confirmó un pedido".
 *
 * Los métodos se llaman desde dentro de @Transactional (OrderService). Si se
 * publicara de inmediato y la transacción luego hiciera rollback, el evento
 * ya habría salido a RabbitMQ para un pedido que nunca quedó guardado. Por
 * eso, si hay una transacción activa, el publish se difiere hasta su
 * afterCommit; si no hay transacción (ej. un test), se publica directo.
 */
@Component
public class OrderEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OrderEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final RabbitMQProperties properties;

    public OrderEventPublisher(RabbitTemplate rabbitTemplate, RabbitMQProperties properties) {
        this.rabbitTemplate = rabbitTemplate;
        this.properties = properties;
    }

    public void publishOrderCreated(Order order) {
        OrderCreatedMessage message = new OrderCreatedMessage(
                order.getId(),
                order.getCustomerId(),
                order.getShippingAddress(),
                order.getTotalAmount(),
                order.getCreatedAt());
        runAfterCommit(() -> doPublishOrderCreated(message));
    }

    public void publishOrderConfirmed(Order order) {
        OrderConfirmedMessage message = new OrderConfirmedMessage(
                order.getId(),
                order.getCustomerId(),
                order.getTotalAmount(),
                order.getUpdatedAt());
        runAfterCommit(() -> doPublishOrderConfirmed(message));
    }

    private void doPublishOrderCreated(OrderCreatedMessage message) {
        rabbitTemplate.convertAndSend(
                properties.getExchange(),
                properties.getRoutingKey().getOrderCreated(),
                message);
        log.info("Evento order.created publicado para el pedido {}", message.orderId());
    }

    private void doPublishOrderConfirmed(OrderConfirmedMessage message) {
        rabbitTemplate.convertAndSend(
                properties.getExchange(),
                properties.getRoutingKey().getOrderConfirmed(),
                message);
        log.info("Evento order.confirmed publicado para el pedido {}", message.orderId());
    }

    private void runAfterCommit(Runnable publishAction) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publishAction.run();
                }
            });
        } else {
            publishAction.run();
        }
    }
}
