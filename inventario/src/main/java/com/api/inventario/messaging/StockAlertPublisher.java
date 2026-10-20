package com.api.inventario.messaging;

import com.api.inventario.config.RabbitMQProperties;
import com.api.inventario.model.Inventory;
import java.time.OffsetDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/*
 * Único punto del dominio inventario que publica alertas a RabbitMQ. Mantiene
 * el RabbitTemplate y los nombres de exchange/routing key fuera de
 * InventoryService, que solo decide cuándo corresponde alertar.
 *
 * El mensaje se arma de inmediato (mientras la sesión de Hibernate sigue
 * abierta, ya que Inventory.product es LAZY), pero el envío real a RabbitMQ
 * se difiere hasta el afterCommit de la transacción activa: así, si la
 * transacción hace rollback, la alerta nunca sale a la cola.
 */
@Component
public class StockAlertPublisher {

    private static final Logger log = LoggerFactory.getLogger(StockAlertPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final RabbitMQProperties properties;

    public StockAlertPublisher(RabbitTemplate rabbitTemplate, RabbitMQProperties properties) {
        this.rabbitTemplate = rabbitTemplate;
        this.properties = properties;
    }

    public void publishStockLow(Inventory inventory, int stockFree) {
        StockLowAlertMessage message = new StockLowAlertMessage(
                inventory.getProduct().getId(),
                inventory.getProduct().getSku(),
                inventory.getStockAvailable(),
                inventory.getStockReserved(),
                stockFree,
                inventory.getReorderPoint(),
                OffsetDateTime.now());
        runAfterCommit(() -> doPublish(message));
    }

    private void doPublish(StockLowAlertMessage message) {
        rabbitTemplate.convertAndSend(
                properties.getExchange(),
                properties.getRoutingKey().getStockLow(),
                message);
        log.info(
                "Alerta inventory.stock.low publicada para el producto {} (stock libre: {}, punto de reposicion: {})",
                message.sku(),
                message.stockFree(),
                message.reorderPoint());
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
