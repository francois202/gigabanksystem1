package gigabank.accountmanagement;

import gigabank.accountmanagement.config.AppConfig;
import gigabank.accountmanagement.service.BankManager;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext(AppConfig.class);

        BankManager bankManager = context.getBean(BankManager.class);

        context.close();
    }
}