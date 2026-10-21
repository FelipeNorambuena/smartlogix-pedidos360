package com.api.rabbitadmin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.api.rabbitadmin.dto.BindingCreateRequest;
import com.api.rabbitadmin.dto.ExchangeCreateRequest;
import com.api.rabbitadmin.dto.ExchangeType;
import com.api.rabbitadmin.dto.QueueCreateRequest;
import com.api.rabbitadmin.dto.QueueResponse;
import com.api.rabbitadmin.exception.ResourceNotFoundException;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.HeadersExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;

/*
 * Pruebas unitarias de RabbitAdminService.
 * Validan que la creacion/eliminacion de colas, exchanges y bindings delega
 * correctamente en AmqpAdmin, y que los recursos inexistentes se reportan
 * como ResourceNotFoundException, sin necesidad de un broker real.
 */
@ExtendWith(MockitoExtension.class)
class RabbitAdminServiceTest {

    @Mock
    private AmqpAdmin amqpAdmin;

    @InjectMocks
    private RabbitAdminService rabbitAdminService;

    @Test
    void createQueue_declaresDurableQueueWithDeadLetterExchange() {
        QueueCreateRequest request = new QueueCreateRequest("ordenes.q", true, "ordenes.dlx");

        QueueResponse response = rabbitAdminService.createQueue(request);

        ArgumentCaptor<Queue> captor = ArgumentCaptor.forClass(Queue.class);
        verify(amqpAdmin).declareQueue(captor.capture());
        Queue declared = captor.getValue();
        assertThat(declared.getName()).isEqualTo("ordenes.q");
        assertThat(declared.isDurable()).isTrue();
        assertThat(declared.getArguments()).containsEntry("x-dead-letter-exchange", "ordenes.dlx");
        assertThat(response.name()).isEqualTo("ordenes.q");
        assertThat(response.deadLetterExchange()).isEqualTo("ordenes.dlx");
    }

    @Test
    void createQueue_withoutDeadLetterExchange_declaresQueueWithoutArgument() {
        QueueCreateRequest request = new QueueCreateRequest("simple.q", false, null);

        rabbitAdminService.createQueue(request);

        ArgumentCaptor<Queue> captor = ArgumentCaptor.forClass(Queue.class);
        verify(amqpAdmin).declareQueue(captor.capture());
        assertThat(captor.getValue().getArguments()).isEmpty();
        assertThat(captor.getValue().isDurable()).isFalse();
    }

    @Test
    void deleteQueue_whenQueueDoesNotExist_throwsResourceNotFoundException() {
        when(amqpAdmin.getQueueProperties("inexistente.q")).thenReturn(null);

        assertThatThrownBy(() -> rabbitAdminService.deleteQueue("inexistente.q"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("inexistente.q");
    }

    @Test
    void deleteQueue_whenQueueExists_delegatesToAmqpAdmin() {
        when(amqpAdmin.getQueueProperties("ordenes.q")).thenReturn(new Properties());

        rabbitAdminService.deleteQueue("ordenes.q");

        verify(amqpAdmin).deleteQueue("ordenes.q");
    }

    @ParameterizedTest
    @EnumSource(ExchangeType.class)
    void createExchange_declaresCorrectExchangeTypeForEachOption(ExchangeType type) {
        ExchangeCreateRequest request = new ExchangeCreateRequest("demo.exchange", type, true);

        rabbitAdminService.createExchange(request);

        ArgumentCaptor<Exchange> captor = ArgumentCaptor.forClass(Exchange.class);
        verify(amqpAdmin).declareExchange(captor.capture());
        Exchange declared = captor.getValue();
        assertThat(declared.getName()).isEqualTo("demo.exchange");
        switch (type) {
            case DIRECT -> assertThat(declared).isInstanceOf(DirectExchange.class);
            case TOPIC -> assertThat(declared).isInstanceOf(TopicExchange.class);
            case FANOUT -> assertThat(declared).isInstanceOf(FanoutExchange.class);
            case HEADERS -> assertThat(declared).isInstanceOf(HeadersExchange.class);
        }
    }

    @Test
    void deleteExchange_whenNotDeleted_throwsResourceNotFoundException() {
        when(amqpAdmin.deleteExchange("inexistente.exchange")).thenReturn(false);

        assertThatThrownBy(() -> rabbitAdminService.deleteExchange("inexistente.exchange"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("inexistente.exchange");
    }

    @Test
    void createBinding_whenQueueDoesNotExist_throwsResourceNotFoundException() {
        BindingCreateRequest request = new BindingCreateRequest("demo.exchange", "inexistente.q", "demo.key");
        when(amqpAdmin.getQueueProperties("inexistente.q")).thenReturn(null);

        assertThatThrownBy(() -> rabbitAdminService.createBinding(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("inexistente.q");
    }

    @Test
    void createBinding_whenQueueExists_declaresBindingWithRoutingKey() {
        BindingCreateRequest request = new BindingCreateRequest("demo.exchange", "ordenes.q", "demo.key");
        when(amqpAdmin.getQueueProperties("ordenes.q")).thenReturn(new Properties());

        rabbitAdminService.createBinding(request);

        ArgumentCaptor<Binding> captor = ArgumentCaptor.forClass(Binding.class);
        verify(amqpAdmin).declareBinding(captor.capture());
        Binding declared = captor.getValue();
        assertThat(declared.getExchange()).isEqualTo("demo.exchange");
        assertThat(declared.getRoutingKey()).isEqualTo("demo.key");
    }
}
