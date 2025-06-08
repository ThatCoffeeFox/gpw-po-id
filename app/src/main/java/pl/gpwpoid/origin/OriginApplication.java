package pl.gpwpoid.origin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OriginApplication {

    public static void main(String[] args) {
        SpringApplication.run(OriginApplication.class, args);
    }
}
