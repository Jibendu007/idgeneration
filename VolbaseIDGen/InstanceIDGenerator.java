package com.volantetech.services.engine.util;

import java.util.UUID;

public class InstanceIDGenerator {
    private static UUID uuid = UUID.randomUUID();
    static String perf_report = System.getenv("PERF_REPORT");

    private InstanceIDGenerator() {

    }

    public static UUID getUUID() {
        return uuid;
    }

    public static String generateRandomID() {
        if (perf_report == null || !(perf_report.equalsIgnoreCase("TRUE"))) {
            return "";
        }
        return UUID.randomUUID().toString();
    }
}
