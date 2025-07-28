package org.example.service;

import org.example.entity.BankAccount;
import org.example.entity.User;

import java.util.List;
import java.util.Map;

/**
 * Сервис отвечает за управление счетами, включая создание, удаление и пополнение
 */
public class BankAccountService {
    private Map<User, List<BankAccount>> userAccounts;
}
