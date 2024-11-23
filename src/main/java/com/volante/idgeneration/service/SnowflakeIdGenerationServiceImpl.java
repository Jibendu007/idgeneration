package com.volante.idgeneration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.volante.idgeneration.model.Payload;
import com.volante.idgeneration.model.UniqueId;
import com.volante.idgeneration.repository.PayloadRepository;
import com.volante.idgeneration.repository.UniqueIdRepository;
import com.volante.idgeneration.util.IDGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class SnowflakeIdGenerationServiceImpl implements IdGenerationService {
    private static final Logger logger = LoggerFactory.getLogger(SnowflakeIdGenerationServiceImpl.class);

    @Autowired
    private PayloadRepository payloadRepository;

    @Autowired
    private VolpayCounterService volpayCounterService;

    @Autowired
    private UniqueIdRepository uniqueIdRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private final IDGenerator idGenerator;

    @Value("${workerId}")
    private long workerId;

    public SnowflakeIdGenerationServiceImpl(IDGenerator idGenerator) {
        this.idGenerator = idGenerator;
        logger.info("Initialized SnowflakeIdGenerationServiceImpl with worker ID: {}", workerId);
    }

    @Override
    public String generateId(String payloadJson) throws IOException {
        logger.debug("Generating ID for payload: {}", payloadJson);

        long id = idGenerator.generateID();
        String hashedId = String.valueOf(id);

        Payload payload = objectMapper.readValue(payloadJson, Payload.class);
        String type = payload.getType();

        logger.debug("Extracted type from payload: {}", type);

        long counter = volpayCounterService.getCounter(type);
        String volpayid = type + "_" + counter;

        logger.info("Generated volpay ID: {} for type: {}", volpayid, type);

        uniqueIdRepository.save(new UniqueId(hashedId, volpayid));
        payloadRepository.save(payload);

        logger.info("Saved ID and payload to repositories. Snowflake ID: {}, Volpay ID: {}",
                hashedId, volpayid);

        return hashedId;
    }
}