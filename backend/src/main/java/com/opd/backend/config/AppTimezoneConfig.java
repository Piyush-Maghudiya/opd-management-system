package com.opd.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.ZoneId;

/**
 * Configuration to manage application timezone.
 * Used to ensure today's appointments are filtered correctly according
 * to the hospital's local calendar day rather than an arbitrary UTC day.
 */
@Configuration
public class AppTimezoneConfig {

    @Value("${app.timezone:Asia/Kolkata}")
    private String timezone;

    @Bean
    public ZoneId applicationZoneId() {
        return ZoneId.of(timezone);
    }
}
