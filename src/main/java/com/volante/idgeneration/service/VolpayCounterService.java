package com.volante.idgeneration.service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
public class VolpayCounterService {
    private final Map<String, Long> counters; // Use a ConcurrentHashMap for thread-safety
    public VolpayCounterService() {
        this.counters = new ConcurrentHashMap<>(); // Initialize with thread-safe map
    }
       public long getCounter(String type) {
        counters.putIfAbsent(type, 1L); // Initialize counter to 1 if not present
        long currentCounter = counters.get(type);
        counters.put(type, currentCounter + 1);
        return currentCounter;
    }
}
