package com.api.rabbitadmin.service;

import com.api.rabbitadmin.dto.BindingCreateRequest;
import com.api.rabbitadmin.dto.BindingResponse;
import com.api.rabbitadmin.dto.ExchangeCreateRequest;
import com.api.rabbitadmin.dto.ExchangeResponse;
import com.api.rabbitadmin.dto.ExchangeType;
import com.api.rabbitadmin.dto.QueueCreateRequest;
import com.api.rabbitadmin.dto.QueueResponse;
import com.api.rabbitadmin.exception.ResourceNotFoundException;
import java.util.Map;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.HeadersExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.stereotype.Service;

/*
 * Unico punto del microservicio que habla con RabbitMQ (via AmqpAdmin).
 * Los controllers solo traducen HTTP <-> DTO y delegan aqui toda la logica
 * de creacion/eliminacion de colas, exchanges y bindings.
 */
@Service
public class RabbitAdminService {

    private final AmqpAdmin amqpAdmin;

    public RabbitAdminService(AmqpAdmin amqpAdmin) {
        this.amqpAdmin = amqpAdmin;
    }

    public QueueResponse createQueue(QueueCreateRequest request) {
        QueueBuilder builder = request.durable() ? QueueBuilder.durable(request.name()) : QueueBuilder.nonDurable(request.name());
        if (request.deadLetterExchange() != null) {
            builder.withArgument("x-dead-letter-exchange", request.deadLetterExchange());
        }
        Queue queue = builder.build();
        amqpAdmin.declareQueue(queue);
        return toQueueResponse(request.name(), request.durable(), request.deadLetterExchange());
    }

    public void deleteQueue(String name) {
        if (amqpAdmin.getQueueProperties(name) == null) {
            throw new ResourceNotFoundException("No existe la cola " + name);
        }
        amqpAdmin.deleteQueue(name);
    }

    public ExchangeResponse createExchange(ExchangeCreateRequest request) {
        Exchange exchange = buildExchange(request.name(), request.type(), request.durable());
        amqpAdmin.declareExchange(exchange);
        return new ExchangeResponse(request.name(), request.type(), request.durable());
    }

    public void deleteExchange(String name) {
        boolean deleted = amqpAdmin.deleteExchange(name);
        if (!deleted) {
            throw new ResourceNotFoundException("No existe el exchange " + name);
        }
    }

    public BindingResponse createBinding(BindingCreateRequest request) {
        if (amqpAdmin.getQueueProperties(request.queue()) == null) {
            throw new ResourceNotFoundException("No existe la cola " + request.queue());
        }
        Binding binding = toBinding(request);
        amqpAdmin.declareBinding(binding);
        return new BindingResponse(request.exchange(), request.queue(), request.routingKey());
    }

    public void deleteBinding(BindingCreateRequest request) {
        Binding binding = toBinding(request);
        amqpAdmin.removeBinding(binding);
    }

    private Binding toBinding(BindingCreateRequest request) {
        return new Binding(
                request.queue(),
                Binding.DestinationType.QUEUE,
                request.exchange(),
                request.routingKey(),
                Map.of());
    }

    private Exchange buildExchange(String name, ExchangeType type, boolean durable) {
        return switch (type) {
            case DIRECT -> new DirectExchange(name, durable, false);
            case TOPIC -> new TopicExchange(name, durable, false);
            case FANOUT -> new FanoutExchange(name, durable, false);
            case HEADERS -> new HeadersExchange(name, durable, false);
        };
    }

    private QueueResponse toQueueResponse(String name, boolean durable, String dlx) {
        return new QueueResponse(name, durable, dlx);
    }
}
