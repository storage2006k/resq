package com.aris.controller;

import com.aris.dto.DispatchRequest;
import com.aris.model.Dispatch;
import com.aris.service.DispatchService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dispatch")
public class DispatchController {

    private final DispatchService dispatchService;

    public DispatchController(DispatchService dispatchService) {
        this.dispatchService = dispatchService;
    }

    @PostMapping
    public ResponseEntity<Dispatch> dispatch(@Valid @RequestBody DispatchRequest request) {
        return ResponseEntity.ok(dispatchService.dispatch(request));
    }
}
