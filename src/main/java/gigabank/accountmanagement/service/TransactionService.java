package gigabank.accountmanagement.service;

import java.util.Set;

/**
 * Сервис отвечает за управление платежами и переводами
 */
public class TransactionService {

    //почему здесь Set, а не List?
    public static Set<String> transactionCategories = Set.of(
            "Health", "Beauty", "Education");


}
