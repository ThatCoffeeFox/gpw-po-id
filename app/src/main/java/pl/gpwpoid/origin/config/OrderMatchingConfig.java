package pl.gpwpoid.origin.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import pl.gpwpoid.origin.models.order.Order;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;

@Configuration
public class OrderMatchingConfig {

    @Bean
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor t = new ThreadPoolTaskExecutor();
        t.setCorePoolSize(4);
        t.setMaxPoolSize(20);
        t.setQueueCapacity(100);
        t.setThreadNamePrefix("OrderMatcher-");
        t.initialize();
        return t;
    }

    @Bean(destroyMethod = "shutdown")
    public ExecutorService orderExecutorService(@Qualifier("taskExecutor") ThreadPoolTaskExecutor taskExecutor) {

        return taskExecutor.getThreadPoolExecutor();
    }

    @Bean
    public ConcurrentMap<Integer, BlockingQueue<Order>> orderQueues() {
        return new ConcurrentHashMap<>();
    }

}
