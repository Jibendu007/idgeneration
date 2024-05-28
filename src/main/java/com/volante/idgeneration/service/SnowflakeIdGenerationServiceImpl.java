package com.volante.idgeneration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.volante.idgeneration.model.Payload;
import com.volante.idgeneration.model.UniqueId;
import com.volante.idgeneration.repository.PayloadRepository;
import com.volante.idgeneration.repository.UniqueIdRepository;
import com.volante.idgeneration.util.IDGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static com.volante.idgeneration.util.IDGenerator.WORKER_ID_BITS;


@Service
public class SnowflakeIdGenerationServiceImpl implements IdGenerationService {

    @Autowired
    private PayloadRepository payloadRepository;// Autowire dependencies
    @Autowired
    private VolpayCounterService volpayCounterService;

    @Autowired
    private UniqueIdRepository uniqueIdRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private final IDGenerator idGenerator;

    @Value("${workerId}") // Inject from application property
    private long workerId;  // Change type to long

    public SnowflakeIdGenerationServiceImpl(IDGenerator idGenerator) {
        this.idGenerator = idGenerator;
        // Validate workerId within allowed range in the constructor
        if (workerId > (1L << WORKER_ID_BITS) - 1L) {
            throw new IllegalArgumentException("Worker ID is larger than allowed range");
        }
        idGenerator.setWorkerId(workerId); // Set worker ID in the generator
    }

    public String generateId(String payloadJson) throws IOException {
        long id = idGenerator.generateID(); // Call without argument
        String hashedId = String.valueOf(id);
        String type = objectMapper.readValue(payloadJson, Payload.class).getType(); // Extract type from payload
        long counter = volpayCounterService.getCounter(type); // Get counter for the type
        String volpayid = type + "_" + counter; // Construct volpayid
        Payload payload = objectMapper.readValue(payloadJson, Payload.class);

        uniqueIdRepository.save(new UniqueId(hashedId, volpayid)); // Store UniqueId with snowflake and volpayid
        payloadRepository.save(payload);

        return hashedId;
    }


    private String convertIdToString(Object id) {
        if (id instanceof Long) {
            return String.valueOf((Long) id);
        } else if (id instanceof String) {
            return (String) id;
        } else {
            // Handle unexpected return type (throw exception or log error)
            throw new RuntimeException("Unexpected ID type from Snowflake: " + id.getClass());
        }
    }
}


