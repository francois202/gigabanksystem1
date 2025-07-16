import gigabank.accountmanagement.entity.Transaction;
import gigabank.accountmanagement.entity.TransactionType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@DisplayName("Проверка паттерна Builder")
public class TransactionTest {

    private final String CATEGORY_SPORT = "Sport";
    private final LocalDateTime CREATE_NOW = LocalDateTime.now();
    private final String AMAZON_STORE = "Amazon Online Store";

    @Test
    @DisplayName("Транзакция с обязательными полями и пустыми полями null")
    void transaction_with_required_fields() {
        Transaction transaction = Transaction.builder()
                .id("1000")
                .value(new BigDecimal("100.00"))
                .type(TransactionType.PAYMENT)
                .build();

        Assertions.assertEquals("1000", transaction.getId());
        Assertions.assertEquals(new BigDecimal("100.00"), transaction.getValue());
        Assertions.assertEquals(TransactionType.PAYMENT, transaction.getType());
        Assertions.assertNull(transaction.getCategory());
        Assertions.assertNull(transaction.getCreatedDate());
        Assertions.assertNull(transaction.getMerchantName());
        Assertions.assertNull(transaction.getMerchantCategoryCode());
        Assertions.assertNull(transaction.getCardNumber());
        Assertions.assertNull(transaction.getBankName());
        Assertions.assertNull(transaction.getDigitalWalletId());
    }

    @Test
    @DisplayName("Транзакция со всеми полями")
    void transaction_with_all_fields() {
        final String CATEGORY_CODE = "5678";
        final String CARD_NUMBER = "5681 9263 1830 1964";
        final String BANK_NAME = "Sber";
        final String DIGITAL_WALLET_ID = "7651991";

        Transaction transaction = Transaction.builder()
                .id("1010")
                .value(new BigDecimal("100.00"))
                .type(TransactionType.PAYMENT)
                .category(CATEGORY_SPORT)
                .createdDate(CREATE_NOW)
                .merchantName(AMAZON_STORE)
                .merchantCategoryCode(CATEGORY_CODE)
                .cardNumber(CARD_NUMBER)
                .bankName(BANK_NAME)
                .digitalWalletId(DIGITAL_WALLET_ID)
                .build();

        Assertions.assertEquals("1010", transaction.getId());
        Assertions.assertEquals(new BigDecimal("100.00"), transaction.getValue());
        Assertions.assertEquals(TransactionType.PAYMENT, transaction.getType());
        Assertions.assertEquals(CATEGORY_SPORT, transaction.getCategory());
        Assertions.assertEquals(CREATE_NOW, transaction.getCreatedDate());
        Assertions.assertEquals(AMAZON_STORE, transaction.getMerchantName());
        Assertions.assertEquals(CATEGORY_CODE, transaction.getMerchantCategoryCode());
        Assertions.assertEquals(CARD_NUMBER, transaction.getCardNumber());
        Assertions.assertEquals(BANK_NAME, transaction.getBankName());
        Assertions.assertEquals(DIGITAL_WALLET_ID, transaction.getDigitalWalletId());

    }
}
