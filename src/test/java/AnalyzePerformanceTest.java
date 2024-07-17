import gigabank.accountmanagement.service.AnalyticsService;
import gigabank.accountmanagement.service.AnalyzePerformance.AnalyzePerformance;
import gigabank.accountmanagement.service.Generate.TransactionTest;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;

public class AnalyzePerformanceTest {
    // Класс с методами для анализа
    private AnalyzePerformance analyzePerformance = new AnalyzePerformance();

    // Тестовый класс транзакции (идентичен оригинальному, внутри инициализируется авто-генератором информации)
    private TransactionTest transactionTest;

    // Список транзакций
    private LinkedList<TransactionTest> transactionTestList = new LinkedList<>();

    /**
     * Создаём 100 тысяч транзакций
     */
    @Before
    public void setUp() throws Exception {
        for (int i = 0; i < 1000000; i++) {
            transactionTest = new TransactionTest();
            transactionTestList.add(transactionTest);
        }
    }

    /**
     * Данный метод выполняет фильтрацию транзакций, так же ограничивает добавление элементов (добавляем ровно половину)
     * и в итоге возвращаем Map<LocalDateTime, List<TransactionTest>>
     *
     * Вариант 1 - тысяча транзакций:  Программа была завершена за - 5 мс без параллельности выполнения;
     *                                 Программа была завершена за - 4мс с параллельным выполнением;
     *
     * Вариант 2 - 10_000 транзакций   Программа была завершена за - 12 мс без параллельности выполнения
     *                                 Программа была завершена за - 9мс с параллельным выполнением
     *
     * Вариант 3 - 100_000 транзакций  Программа была завершена за - 29 мс без параллельности выполнения
     *                                 Программа была завершена за - 41мс с параллельным выполнением
     *
     * Вариант 4 - 1_0000000 транзакций Программа была завершена за - 98 мс без параллельности выполнения
     *                                  Программа была завершена за - 180мс с параллельным выполнением
     *
     * В данном случае, увеличение числа транзакций влияет на параллельное выполнение, чем больше элементов, тем дольше
     * выполняется программа. Предполагаю, потому что у нас есть метод limit, который является stateful, то есть, ему надо
     * знать о других элементах в потоке
     */
    @Test
    public void AnalyzePerformanceOne() throws Exception {
        analyzePerformance.analyzePerformancesToMap(transactionTestList);
    }

    /**
     * Данный метод выполняет фильтрацию транзакций, и получение average их значений, возвращает  double
     *
     * Вариант 1 - тысяча транзакций:  Программа была завершена за - 3мс без параллельности выполнения
     *                                 Программа была завершена за - 1мс с параллельным выполнением
     *
     * Вариант 2 - 10_000 транзакций   Программа была завершена за - 7мс без параллельности выполнения
     *                                 Программа была завершена за - 4мс с параллельным выполнением
     *
     * Вариант 3 - 100_000 транзакций  Программа была завершена за - 16мс без параллельности выполнения
     *                                 Программа была завершена за - 12мс с параллельным выполнением
     *
     * Вариант 4 - 1_0000000 транзакций Программа была завершена за - 97мс без параллельности выполнения
     *                                  Программа была завершена за - 78мс с параллельным выполнением
     *
     * В данном случае, метод показывает, что параллельное выполнение выполняется быстрее, независимо от
     * количества транзакций. В данном случае у нас нет stateful методов
     */
    @Test
    public void AnalyzePerformanceTwo() throws Exception {
        analyzePerformance.analyzePerformancesToDouble(transactionTestList);
    }
}
