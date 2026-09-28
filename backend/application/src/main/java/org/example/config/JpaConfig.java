package org.example.config;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = {
        "org.identity.repository",
        "org.processing.repository",
        "org.streaming.repository"
})
@EntityScan(basePackages = {
        "org.identity.entity",
        "org.processing.entity",
        "org.streaming.entity"
})
public class JpaConfig {
}
