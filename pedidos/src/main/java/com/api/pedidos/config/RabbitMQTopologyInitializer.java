package com.api.pedidos.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/*
 * Fuerza la declaración explícita de la topología RabbitMQ (exchanges, colas,
 * bindings) al arrancar. RabbitAdmin ya declara automáticamente al recibir
 * ContextRefreshedEvent, pero esa señal no siempre llega a tiempo en este
 * entorno (Spring Boot 4 / Spring AMQP 4); esta llamada explícita es
 * idempotente y garantiza que la topología quede creada sin depender de ese
 * orden de eventos.
 */
@Component
public class RabbitMQTopologyInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQTopologyInitializer.class);

    private final AmqpAdmin amqpAdmin;

    public RabbitMQTopologyInitializer(AmqpAdmin amqpAdmin) {
        this.amqpAdmin = amqpAdmin;
    }

    @Override
    public void run(ApplicationArguments args) {
        amqpAdmin.initialize();
        log.info("Topología RabbitMQ (exchanges, colas y bindings) declarada correctamente.");
    }
}
