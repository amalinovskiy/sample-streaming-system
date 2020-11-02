package com.ocado.presentation.streaming.producer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.logging.Logger;

@SpringBootApplication
public class Producer {

    private static final Logger logger = Logger.getLogger(Producer.class.getCanonicalName());

    public static void main(String[] args) {
        SpringApplication.run(Producer.class, args);
    }

    @Bean
    Supplier<DomainEvent> produce() {
        return () -> {
            logger.info("Sending new event");
            var random = new Random(42);
            int delay = random.nextInt(1000);
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return new DomainEvent("EventType", UUID.randomUUID().toString());
        };
    }
}
