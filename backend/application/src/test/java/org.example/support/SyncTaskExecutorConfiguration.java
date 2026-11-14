package org.example.support;

import java.util.concurrent.Executor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;

@TestConfiguration(proxyBeanMethods = false)
public class SyncTaskExecutorConfiguration {

    @Bean
    public AsyncConfigurer syncAsyncConfigurer() {
        return new AsyncConfigurer() {
            @Override
            public Executor getAsyncExecutor() {
                return new SyncTaskExecutor();
            }
        };
    }
}
