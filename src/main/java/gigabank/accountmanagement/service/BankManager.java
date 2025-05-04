package gigabank.accountmanagement.service;

import gigabank.accountmanagement.dto.UserRequest;
import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.service.notification.ExternalNotificationService;

import java.util.List;

public class BankManager {
    private BankAccountService serv = new BankAccountService(null, null); // неправильная инициализация
    private PaymentGatewayService payService = new PaymentGatewayService();
    private ExternalNotificationService notifier = new ExternalNotificationService();

    public void doWork(List<UserRequest> rqs) {
        for (UserRequest r : rqs) {
            BankAccount a = serv.findAccountById(r.getAccountId());
            if (a == null) {
                System.out.println("no acc " + r.getAccountId());
                continue;
            }
            if (r.getPaymentType().equals("CARD")) {
                payService.authorize("tx", r.getAmount());
                serv.withdraw(a, r.getAmount());
                System.out.println("card pay " + a.getId());
                notifier.sendSms(a.getOwner().getPhoneNumber(), "paid " + r.getAmount());
                notifier.sendEmail(a.getOwner().getEmail(), "payment", "card pay " + r.getAmount());
            } else if (r.getPaymentType().equals("BANK")) {
                payService.authorize("tx", r.getAmount());
                System.out.println("bank pay " + a.getId());
                notifier.sendSms(a.getOwner().getPhoneNumber(), "payment bank " + r.getAmount());
                notifier.sendEmail(a.getOwner().getEmail(), "payment bank", "payment bank " + r.getAmount());
            } else if (r.getPaymentType().equals("WALLET")) {
                payService.authorize("tx", r.getAmount());
                serv.withdraw(a, r.getAmount());
                System.out.println("wallet pay " + a.getId());
                notifier.sendSms(a.getOwner().getPhoneNumber(), "wallet " + r.getAmount());
                notifier.sendEmail(a.getOwner().getEmail(), "wallet", "wallet " + r.getAmount());
            }
        }
    }
}