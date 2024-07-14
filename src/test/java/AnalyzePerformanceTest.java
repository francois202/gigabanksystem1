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
     *
     */
    @Before
    public void setUp() throws Exception {
        for (int i = 0; i < 100_000; i++) {
            transactionTest = new TransactionTest();
            transactionTestList.add(transactionTest);
        }
    }

    @Test
    public void test() throws Exception {
        analyticsService.analyzePerformances(transactionTestList);
    }
}
