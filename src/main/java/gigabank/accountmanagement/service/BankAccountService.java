package gigabank.accountmanagement.service;

import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.entity.User;

import java.util.List;
import java.util.Map;

/**
 * Сервис отвечает за управление счетами, включая создание, удаление и пополнение
 */
public class BankAccountService {
    private Map<User, List<BankAccount>> userAccounts;
}
