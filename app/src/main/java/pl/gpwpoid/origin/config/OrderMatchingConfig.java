package pl.gpwpoid.origin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import pl.gpwpoid.origin.models.order.Order;

import java.util.concurrent.*;

@Configuration
public class OrderMatchingConfig {

    @Bean
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor t = new ThreadPoolTaskExecutor();
        t.setCorePoolSize(4);
        t.setMaxPoolSize(20);
        t.setQueueCapacity(100);
        t.setThreadNamePrefix("OrderMatcher-");
        t.initialize();  // IMPORTANT, so the pool is actually created
        return t;
    }

    @Bean(destroyMethod = "shutdown")
    public ExecutorService orderExecutorService(ThreadPoolTaskExecutor taskExecutor) {
        // get the native JDK ExecutorService
        return taskExecutor.getThreadPoolExecutor();
    }

    @Bean
    public ConcurrentMap<Integer, BlockingQueue<Order>> orderQueues() {
        return new ConcurrentHashMap<>();
    }

}
