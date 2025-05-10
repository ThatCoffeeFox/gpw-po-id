package pl.gpwpoid.origin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class ExecutorConfig {

    @Bean(name = "orderMatcherExecutor")
    public Executor orderMatcherExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(4); // minimalna liczba wątków
        executor.setMaxPoolSize(20); // maksymalna liczba wątków
        executor.setQueueCapacity(100); // ile zadań może czekać w kolejce
        executor.setThreadNamePrefix("OrderMatcher-");
        executor.initialize();

        return executor;
    }
}
