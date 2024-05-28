package com.volante.idgeneration.controller;

import com.volante.idgeneration.service.IdGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/idgen")
public class PayloadController {

    @Autowired
    private IdGenerationService idGenerationService;

    @PostMapping
    public ResponseEntity<String> generateId(@RequestBody String payloadJson) throws IOException {
        String generatedId = idGenerationService.generateId(payloadJson);
        return ResponseEntity.ok("Generated ID: " + generatedId);
    }
}
