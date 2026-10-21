package com.api.rabbitadmin.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.api.rabbitadmin.dto.QueueResponse;
import com.api.rabbitadmin.exception.ResourceNotFoundException;
import com.api.rabbitadmin.service.RabbitAdminService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/*
 * Prueba que la API REST de rabbit-admin valida las entradas (indicador de
 * la pauta: no se pueden crear colas/exchanges/bindings con nombres vacios
 * o configuraciones invalidas) y que los recursos inexistentes devuelven
 * 404 en vez de un error generico.
 */
@WebMvcTest(controllers = {QueueController.class, ExchangeController.class, BindingController.class})
class RabbitAdminControllerValidationTest {

    @org.springframework.beans.factory.annotation.Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RabbitAdminService rabbitAdminService;

    @Test
    void createQueue_blankName_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/queues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"durable\":true}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createQueue_invalidCharacters_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/queues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"cola con espacios!\",\"durable\":true}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createQueue_validRequest_returnsCreated() throws Exception {
        when(rabbitAdminService.createQueue(any())).thenReturn(new QueueResponse("ordenes.q", true, null));

        mockMvc.perform(post("/queues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"ordenes.q\",\"durable\":true}"))
                .andExpect(status().isCreated());
    }

    @Test
    void deleteQueue_whenServiceReportsNotFound_returnsNotFound() throws Exception {
        org.mockito.Mockito.doThrow(new ResourceNotFoundException("No existe la cola inexistente.q"))
                .when(rabbitAdminService).deleteQueue(eq("inexistente.q"));

        mockMvc.perform(delete("/queues/inexistente.q"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createExchange_missingType_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/exchanges")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"demo.exchange\",\"durable\":true}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createExchange_blankName_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/exchanges")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"type\":\"TOPIC\",\"durable\":true}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBinding_blankQueue_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/bindings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"exchange\":\"demo.exchange\",\"queue\":\"\",\"routingKey\":\"demo.key\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBinding_whenServiceReportsQueueNotFound_returnsNotFound() throws Exception {
        when(rabbitAdminService.createBinding(any()))
                .thenThrow(new ResourceNotFoundException("No existe la cola inexistente.q"));

        mockMvc.perform(post("/bindings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"exchange\":\"demo.exchange\",\"queue\":\"inexistente.q\",\"routingKey\":\"demo.key\"}"))
                .andExpect(status().isNotFound());
    }
}
