package com.api.rabbitadmin.controller;

import com.api.rabbitadmin.dto.BindingCreateRequest;
import com.api.rabbitadmin.dto.BindingResponse;
import com.api.rabbitadmin.service.RabbitAdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bindings")
public class BindingController {

    private final RabbitAdminService rabbitAdminService;

    public BindingController(RabbitAdminService rabbitAdminService) {
        this.rabbitAdminService = rabbitAdminService;
    }

    @PostMapping
    public ResponseEntity<BindingResponse> createBinding(@Valid @RequestBody BindingCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rabbitAdminService.createBinding(request));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteBinding(@Valid @RequestBody BindingCreateRequest request) {
        rabbitAdminService.deleteBinding(request);
        return ResponseEntity.noContent().build();
    }
}
