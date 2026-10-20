package com.api.inventario.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
 * Declara la topología RabbitMQ del dominio inventario: un exchange topic
 * para alertas de stock (permite patrones como inventory.stock.*) y su
 * exchange/cola de dead-letter asociados. Los nombres vienen siempre de
 * RabbitMQProperties, nunca hardcodeados aquí ni en las clases de negocio.
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
    public TopicExchange inventarioExchange() {
        return new TopicExchange(properties.getExchange(), true, false);
    }

    @Bean
    public TopicExchange inventarioDlxExchange() {
        return new TopicExchange(properties.getDlxExchange(), true, false);
    }

    @Bean
    public Queue alertasQueue() {
        return QueueBuilder.durable(properties.getQueue().getAlertas())
                .withArgument("x-dead-letter-exchange", properties.getDlxExchange())
                .build();
    }

    @Bean
    public Queue alertasDlq() {
        return QueueBuilder.durable(properties.getQueue().getAlertasDlq()).build();
    }

    @Bean
    public Binding bindAlertas() {
        return BindingBuilder.bind(alertasQueue())
                .to(inventarioExchange())
                .with(properties.getRoutingKey().getStockLow());
    }

    @Bean
    public Binding bindAlertasDlq() {
        return BindingBuilder.bind(alertasDlq())
                .to(inventarioDlxExchange())
                .with(properties.getRoutingKey().getStockLow());
    }
}
