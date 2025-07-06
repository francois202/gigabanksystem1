import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.entity.Transaction;
import gigabank.accountmanagement.entity.TransactionBuilder;
import gigabank.accountmanagement.entity.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class TransactionBuilderTest {
    private static final String ID = "1";
    private static final BigDecimal VALUE = new BigDecimal("50.00");
    private static final TransactionType TYPE = TransactionType.PAYMENT;
    private static final String CATEGORY = "Food";
    private static final BankAccount BANK_ACCOUNT = new BankAccount("account1", new ArrayList<>());
    private static final LocalDateTime CREATED_DATE = LocalDateTime.now();
    private static final String MERCHANT_NAME = "Grocery Store";
    private static final String MERCHANT_CATEGORY_CODE = "5411";
    private static final String CARD_NUMBER = "1234-5678-9012-3456";
    private static final String BANK_NAME = "GigaBank";
    private static final String DIGITAL_WALLET_ID = "wallet123";

    @Test
    @DisplayName("Проверяет создание транзакции только с обязательными полями")
    public void testBuildTransactionWithRequiredFieldsOnly() {
        Transaction transaction = Transaction.builder()
                .id(ID)
                .value(VALUE)
                .type(TYPE)
                .category(CATEGORY)
                .bankAccount(BANK_ACCOUNT)
                .createdDate(CREATED_DATE)
                .build();

        assertEquals(ID, transaction.getId(), "ID должен быть установлен");
        assertEquals(VALUE, transaction.getValue(), "Value должен быть установлен");
        assertEquals(TYPE, transaction.getType(), "Type должен быть установлен");
        assertEquals(CATEGORY, transaction.getCategory(), "Category должна быть установлена");
        assertEquals(BANK_ACCOUNT, transaction.getBankAccount(), "BankAccount должен быть установлен");
        assertEquals(CREATED_DATE, transaction.getCreatedDate(), "CreatedDate должен быть установлен");

        assertNull(transaction.getMerchantName(), "MerchantName должен быть null");
        assertNull(transaction.getMerchantCategoryCode(), "MerchantCategoryCode должен быть null");
        assertNull(transaction.getCardNumber(), "CardNumber должен быть null");
        assertNull(transaction.getBankName(), "BankName должен быть null");
        assertNull(transaction.getDigitalWalletId(), "DigitalWalletId должен быть null");
    }

    @Test
    @DisplayName("Проверяет создание транзакции со всеми полями")
    public void testBuildTransactionWithAllFields() {
        Transaction transaction = Transaction.builder()
                .id(ID)
                .value(VALUE)
                .type(TYPE)
                .category(CATEGORY)
                .bankAccount(BANK_ACCOUNT)
                .createdDate(CREATED_DATE)
                .merchantName(MERCHANT_NAME)
                .merchantCategoryCode(MERCHANT_CATEGORY_CODE)
                .cardNumber(CARD_NUMBER)
                .bankName(BANK_NAME)
                .digitalWalletId(DIGITAL_WALLET_ID)
                .build();

        assertEquals(ID, transaction.getId(), "ID должен быть установлен");
        assertEquals(VALUE, transaction.getValue(), "Value должен быть установлен");
        assertEquals(TYPE, transaction.getType(), "Type должен быть установлен");
        assertEquals(CATEGORY, transaction.getCategory(), "Category должна быть установлена");
        assertEquals(BANK_ACCOUNT, transaction.getBankAccount(), "BankAccount должен быть установлен");
        assertEquals(CREATED_DATE, transaction.getCreatedDate(), "CreatedDate должен быть установлен");
        assertEquals(MERCHANT_NAME, transaction.getMerchantName(), "MerchantName должен быть установлен");
        assertEquals(MERCHANT_CATEGORY_CODE, transaction.getMerchantCategoryCode(), "MerchantCategoryCode должен быть установлен");
        assertEquals(CARD_NUMBER, transaction.getCardNumber(), "CardNumber должен быть установлен");
        assertEquals(BANK_NAME, transaction.getBankName(), "BankName должен быть установлен");
        assertEquals(DIGITAL_WALLET_ID, transaction.getDigitalWalletId(), "DigitalWalletId должен быть установлен");
    }

    @Test
    @DisplayName("Проверяет создание транзакции с обязательными и одним необязательным полем")
    public void testBuildTransactionWithOptionalFieldsNull() {
        Transaction transaction = Transaction.builder()
                .id(ID)
                .value(VALUE)
                .type(TYPE)
                .category(CATEGORY)
                .bankAccount(BANK_ACCOUNT)
                .createdDate(CREATED_DATE)
                .merchantName(MERCHANT_NAME)
                .build();

        assertEquals(ID, transaction.getId(), "ID должен быть установлен");
        assertEquals(VALUE, transaction.getValue(), "Value должен быть установлен");
        assertEquals(TYPE, transaction.getType(), "Type должен быть установлен");
        assertEquals(CATEGORY, transaction.getCategory(), "Category должна быть установлена");
        assertEquals(BANK_ACCOUNT, transaction.getBankAccount(), "BankAccount должен быть установлен");
        assertEquals(CREATED_DATE, transaction.getCreatedDate(), "CreatedDate должен быть установлен");

        assertEquals(MERCHANT_NAME, transaction.getMerchantName(), "MerchantName должен быть установлен");
        assertNull(transaction.getMerchantCategoryCode(), "MerchantCategoryCode должен быть null");
        assertNull(transaction.getCardNumber(), "CardNumber должен быть null");
        assertNull(transaction.getBankName(), "BankName должен быть null");
        assertNull(transaction.getDigitalWalletId(), "DigitalWalletId должен быть null");
    }
}