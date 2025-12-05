package gigabank.accountmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GigaBankApplication {
    public static void main(String[] args) {
        SpringApplication.run(GigaBankApplication.class, args);
    }
}