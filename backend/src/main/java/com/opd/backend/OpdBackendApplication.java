package com.opd.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the OPD Management Module Spring Boot application.
 *
 * @SpringBootApplication enables:
 * 1. @Configuration: Tags the class as a source of bean definitions.
 * 2. @EnableAutoConfiguration: Tells Spring Boot to configure beans based on classpath settings.
 * 3. @ComponentScan: Automatically scans packages for @Component, @Service, @Repository, and @RestController.
 */
@SpringBootApplication
public class OpdBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpdBackendApplication.class, args);
    }
}
