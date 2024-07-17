package gigabank.test.analyticsservice;

import org.junit.jupiter.api.Test;
import static gigabank.test.TestUtils.*;

public class AnalyzePerformanceTest {

    @Test
    void outputResultAnalyzePerformance() {
        for (String result : analyticsService.analyzePerformance(generateTransactions(1_000_000))) {
            System.out.println(result);
        }
        System.out.println("--------------------------------------");
    }
}