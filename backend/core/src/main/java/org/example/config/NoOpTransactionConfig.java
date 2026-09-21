package org.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class NoOpTransactionConfig {

    @Bean
    PlatformTransactionManager transactionManager() {
        return new NoOpTransactionManager();
    }
}
