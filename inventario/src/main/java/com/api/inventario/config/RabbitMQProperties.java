package com.api.inventario.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/*
 * Punto único de verdad para los nombres de exchanges, colas y routing keys
 * del dominio inventario. Se completa desde application.properties (prefijo
 * app.rabbitmq) para evitar valores sueltos repartidos en el código de negocio.
 */
@ConfigurationProperties(prefix = "app.rabbitmq")
public class RabbitMQProperties {

    private String exchange;
    private String dlxExchange;
    private RoutingKey routingKey = new RoutingKey();
    private Queue queue = new Queue();

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getDlxExchange() {
        return dlxExchange;
    }

    public void setDlxExchange(String dlxExchange) {
        this.dlxExchange = dlxExchange;
    }

    public RoutingKey getRoutingKey() {
        return routingKey;
    }

    public void setRoutingKey(RoutingKey routingKey) {
        this.routingKey = routingKey;
    }

    public Queue getQueue() {
        return queue;
    }

    public void setQueue(Queue queue) {
        this.queue = queue;
    }

    public static class RoutingKey {
        private String stockLow;

        public String getStockLow() {
            return stockLow;
        }

        public void setStockLow(String stockLow) {
            this.stockLow = stockLow;
        }
    }

    public static class Queue {
        private String alertas;
        private String alertasDlq;

        public String getAlertas() {
            return alertas;
        }

        public void setAlertas(String alertas) {
            this.alertas = alertas;
        }

        public String getAlertasDlq() {
            return alertasDlq;
        }

        public void setAlertasDlq(String alertasDlq) {
            this.alertasDlq = alertasDlq;
        }
    }
}
