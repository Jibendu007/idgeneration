package com.volante.idgeneration.Configuration;

import com.volante.idgeneration.service.VolpayCounterService;
import com.volante.idgeneration.util.IDGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {

    @Value("${workerId}")
    private long workerId;

    @Bean
    public IDGenerator idGenerator() {
        return new IDGenerator(workerId);
    }
    @Bean
    public VolpayCounterService volpayCounterService() {
        return new VolpayCounterService(); // Create a new instance
    }
}
