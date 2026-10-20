package com.api.pedidos.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
 * Declara la topología RabbitMQ del dominio pedidos: un exchange direct para
 * los eventos de negocio (order.created, order.confirmed) y su exchange/colas
 * de dead-letter asociados. Los nombres vienen siempre de RabbitMQProperties,
 * nunca hardcodeados aquí ni en las clases de negocio.
 */
@Configuration
@EnableConfigurationProperties(RabbitMQProperties.class)
public class RabbitMQConfig {

    private final RabbitMQProperties properties;

    public RabbitMQConfig(RabbitMQProperties properties) {
        this.properties = properties;
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public DirectExchange pedidosExchange() {
        return new DirectExchange(properties.getExchange(), true, false);
    }

    @Bean
    public DirectExchange pedidosDlxExchange() {
        return new DirectExchange(properties.getDlxExchange(), true, false);
    }

    @Bean
    public Queue notificacionesQueue() {
        return QueueBuilder.durable(properties.getQueue().getNotificaciones())
                .withArgument("x-dead-letter-exchange", properties.getDlxExchange())
                .build();
    }

    @Bean
    public Queue notificacionesDlq() {
        return QueueBuilder.durable(properties.getQueue().getNotificacionesDlq()).build();
    }

    @Bean
    public Queue documentosQueue() {
        return QueueBuilder.durable(properties.getQueue().getDocumentos())
                .withArgument("x-dead-letter-exchange", properties.getDlxExchange())
                .build();
    }

    @Bean
    public Queue documentosDlq() {
        return QueueBuilder.durable(properties.getQueue().getDocumentosDlq()).build();
    }

    @Bean
    public Binding bindNotificaciones() {
        return BindingBuilder.bind(notificacionesQueue())
                .to(pedidosExchange())
                .with(properties.getRoutingKey().getOrderCreated());
    }

    @Bean
    public Binding bindNotificacionesDlq() {
        return BindingBuilder.bind(notificacionesDlq())
                .to(pedidosDlxExchange())
                .with(properties.getRoutingKey().getOrderCreated());
    }

    @Bean
    public Binding bindDocumentos() {
        return BindingBuilder.bind(documentosQueue())
                .to(pedidosExchange())
                .with(properties.getRoutingKey().getOrderConfirmed());
    }

    @Bean
    public Binding bindDocumentosDlq() {
        return BindingBuilder.bind(documentosDlq())
                .to(pedidosDlxExchange())
                .with(properties.getRoutingKey().getOrderConfirmed());
    }
}
