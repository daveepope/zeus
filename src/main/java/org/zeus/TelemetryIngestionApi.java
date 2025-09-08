package org.zeus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class TelemetryIngestionApi {
    public static void main(String[] args) {
        SpringApplication.run(TelemetryIngestionApi.class, args);
    }
}