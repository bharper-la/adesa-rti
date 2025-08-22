
package com.la.dataservices.adesa_rti;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SyncTaskExecutor;

import java.util.concurrent.Executor;

@TestConfiguration
public class TestAsyncConfig {
    @Bean(name = "importExecutor")
    public Executor importExecutor() {
        // Run @Async work on the calling thread during tests
        return new SyncTaskExecutor();
    }
}
