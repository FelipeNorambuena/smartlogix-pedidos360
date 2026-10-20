package com.api.pedidos.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/*
 * Punto único de verdad para los nombres de exchanges, colas y routing keys
 * del dominio pedidos. Se completa desde application.properties (prefijo app.rabbitmq)
 * para evitar valores sueltos repartidos en el código de negocio.
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
        private String orderCreated;
        private String orderConfirmed;

        public String getOrderCreated() {
            return orderCreated;
        }

        public void setOrderCreated(String orderCreated) {
            this.orderCreated = orderCreated;
        }

        public String getOrderConfirmed() {
            return orderConfirmed;
        }

        public void setOrderConfirmed(String orderConfirmed) {
            this.orderConfirmed = orderConfirmed;
        }
    }

    public static class Queue {
        private String notificaciones;
        private String notificacionesDlq;
        private String documentos;
        private String documentosDlq;

        public String getNotificaciones() {
            return notificaciones;
        }

        public void setNotificaciones(String notificaciones) {
            this.notificaciones = notificaciones;
        }

        public String getNotificacionesDlq() {
            return notificacionesDlq;
        }

        public void setNotificacionesDlq(String notificacionesDlq) {
            this.notificacionesDlq = notificacionesDlq;
        }

        public String getDocumentos() {
            return documentos;
        }

        public void setDocumentos(String documentos) {
            this.documentos = documentos;
        }

        public String getDocumentosDlq() {
            return documentosDlq;
        }

        public void setDocumentosDlq(String documentosDlq) {
            this.documentosDlq = documentosDlq;
        }
    }
}
