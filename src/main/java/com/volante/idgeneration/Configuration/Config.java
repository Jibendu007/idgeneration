package com.volante.idgeneration.Configuration;

import com.volante.idgeneration.service.VolpayCounterService;
import com.volante.idgeneration.util.IDGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class Config {

    @Value("${workerId}")
    private long workerId;

    @Bean
    public IDGenerator idGenerator() {
        return new IDGenerator(workerId);
    }

    @Bean
    public VolpayCounterService volpayCounterService() {
        return new VolpayCounterService();
    }

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("IdGen-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();
        return executor;
    }
}