package org.identity.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@Profile("test")
public class NoOpTransactionConfig {

    @Bean
    PlatformTransactionManager transactionManager() {
        return new NoOpTransactionManager();
    }
}
