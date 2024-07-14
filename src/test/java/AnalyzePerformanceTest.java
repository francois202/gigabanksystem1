import gigabank.accountmanagement.service.AnalyticsService;
import gigabank.accountmanagement.service.Generate.TransactionTest;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;

public class AnalyzePerformanceTest {
    private AnalyticsService analyticsService = new AnalyticsService();
    private TransactionTest transactionTest;
    private LinkedList<TransactionTest> transactionTestList = new LinkedList<>();

    /**
     * Создаём 100 тысяч транзакций
     */
    @Before
    public void setUp() throws Exception {
        for (int i = 0; i < 10_00000; i++) {
            transactionTest = new TransactionTest();
            transactionTestList.add(transactionTest);
        }
    }

    /**
     * Выводит количество времени, потраченное на последовательное и параллельное выполнение stream
     * Результат выполнения: Каждый вызов мы получаем разный результат, где последовательное и параллельное выполнение
     * выполняется по разному....
     * Чем больше элементов мы обрабатываем, тем эффективнее работает параллельное выполнение, но опять же, некоторые вызовы
     * показывают, что последовательное выполнение может быть быстрее
     */
    @Test
    public void test() throws Exception {
        analyticsService.analyzePerformances(transactionTestList);
    }
}
