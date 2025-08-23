package gigabank.accountmanagement.service.dbimpl;

import gigabank.accountmanagement.db.DBManager;
import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.entity.Transaction;
import gigabank.accountmanagement.enums.TransactionType;
import gigabank.accountmanagement.service.BankAccountService;
import gigabank.accountmanagement.service.notification.NotificationService;
import gigabank.accountmanagement.service.payment.PaymentGatewayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;

@Service
public class BankAccountServiceBDImpl extends BankAccountService {
    private final DBManager dbManager;

    @Autowired
    public BankAccountServiceBDImpl(NotificationService notificationService,
                                    PaymentGatewayService paymentGatewayService,
                                    DBManager dbManager) {
        super(notificationService, paymentGatewayService);
        this.dbManager = dbManager;
    }

    @Override
    public void deposit(BankAccount account, BigDecimal amount) {
        BigDecimal newBalance = account.getBalance().add(amount);
        try {
            dbManager.updateBalance(account.getId(), newBalance);

            Transaction transaction = Transaction.builder()
                    .id(String.valueOf(System.currentTimeMillis()))
                    .value(amount)
                    .type(TransactionType.valueOf("DEPOSIT"))
                    .createdDate(LocalDateTime.now())
                    .build();

            dbManager.addTransaction(transaction);

            account.setBalance(newBalance);
        }
        catch (Exception e) {
            throw new RuntimeException("Не удалось пополнить счет", e);
        }
    }


}
