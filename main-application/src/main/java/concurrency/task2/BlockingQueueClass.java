package concurrency.task2;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Демонстрация работы Producer-Consumer с использованием BlockingQueue.
 * Продюсер производит числа, консьюмер потребляет их с разной скоростью.
 */
public class BlockingQueueClass {
    public static void main(String[] args) throws InterruptedException {
        BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(5);
        AtomicBoolean producerFinished = new AtomicBoolean(false);

        System.out.println("Размер очереди: 5 элементов");
        System.out.println("Продюсер: 100мс, Консьюмер: 200мс\n");

        Thread producer = new Thread(() -> {
            try {
                for (int i = 1; i <= 100; i++) {
                    queue.put(i);
                    System.out.printf("[Producer] Добавил: %3d | Размер очереди: %d%n", i, queue.size());
                    Thread.sleep(100);
                }
                producerFinished.set(true);
                System.out.println("\n[Producer] ЗАВЕРШИЛ РАБОТУ - произведено 100 чисел");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("[Producer] Прерван");
            }
        });

        Thread consumer = new Thread(() -> {
            int consumedCount = 0;
            try {
                while (true) {
                    Integer number = queue.poll(500, java.util.concurrent.TimeUnit.MILLISECONDS);

                    if (number != null) {
                        consumedCount++;
                        System.out.printf("[Consumer] Забрал: %3d | Очередь: %d | Всего: %d%n",
                                number, queue.size(), consumedCount);
                        Thread.sleep(200); 
                    } else {
                        if (producerFinished.get()) {
                            System.out.println("\n[Consumer] ЗАВЕРШИЛ РАБОТУ - очередь пуста и продюсер завершен");
                            break;
                        } else {
                            System.out.println("[Consumer] В ожидании новых элементов... (очередь пуста)");
                        }
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("[Consumer] Прерван");
            }
        });

        producer.start();
        consumer.start();

        producer.join();
        consumer.join();

        System.out.println("\nИтоговый размер очереди: " + queue.size());
    }
}