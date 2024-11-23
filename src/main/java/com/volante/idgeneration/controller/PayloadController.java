package com.volante.idgeneration.controller;

import com.volante.idgeneration.service.IdGenerationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/idgen")
public class PayloadController {
    private static final Logger logger = LoggerFactory.getLogger(PayloadController.class);

    @Autowired
    private IdGenerationService idGenerationService;

    @Autowired
    @Qualifier("taskExecutor")
    private Executor taskExecutor;

    @PostMapping
    public CompletableFuture<ResponseEntity<String>> generateId(@RequestBody String payloadJson) {
        logger.info("Request received on thread: {}", Thread.currentThread().getName());
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Processing request on thread: {}", Thread.currentThread().getName());
            try {
                String generatedId = idGenerationService.generateId(payloadJson);
                logger.info("Generated ID {} on thread: {}", generatedId, Thread.currentThread().getName());
                return ResponseEntity.ok("Generated ID: " + generatedId);
            } catch (IOException e) {
                logger.error("Error generating ID on thread: {}", Thread.currentThread().getName(), e);
                throw new CompletionException(e);
            }
        }, taskExecutor);
    }

    @PostMapping("/batch")
    public CompletableFuture<ResponseEntity<List<String>>> generateBatchIds(@RequestBody List<String> payloadJsonList) {
        logger.info("Batch request received with {} items on thread: {}",
                payloadJsonList.size(), Thread.currentThread().getName());

        List<CompletableFuture<String>> futures = payloadJsonList.stream()
                .map(payload -> CompletableFuture.supplyAsync(() -> {
                    logger.info("Processing batch item on thread: {}", Thread.currentThread().getName());
                    try {
                        String id = idGenerationService.generateId(payload);
                        logger.info("Generated batch ID {} on thread: {}", id, Thread.currentThread().getName());
                        return id;
                    } catch (IOException e) {
                        logger.error("Error generating batch ID on thread: {}", Thread.currentThread().getName(), e);
                        throw new CompletionException(e);
                    }
                }, taskExecutor))
                .collect(Collectors.toList());

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList()))
                .thenApply(ResponseEntity::ok);
    }
}