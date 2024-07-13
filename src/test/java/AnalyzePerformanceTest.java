import gigabank.accountmanagement.service.AnalyticsService;
import gigabank.accountmanagement.service.Generate.GenerateTransactions;
import gigabank.accountmanagement.service.Generate.TransactionTest;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class AnalyzePerformanceTest {
    private AnalyticsService analyticsService = new AnalyticsService();
    private GenerateTransactions generateTransactions = new GenerateTransactions();
    private TransactionTest transactionTest;
    private LinkedList<TransactionTest> transactionTestList = new LinkedList<>();

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
