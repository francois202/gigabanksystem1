package concurrency.task1;

import lombok.Getter;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Класс для демонстрации различных подходов к синхронизации в многопоточной среде.
 * Содержит несколько счетчиков, каждый из которых использует свой метод синхронизации.
 */
@Getter
public class Counter {
    private int basicCount = 0;
    private int syncMethodCount = 0;
    private int syncBlockCount = 0;
    private int reentrantCount = 0;
    private AtomicInteger atomicCount = new AtomicInteger(0);

    private final Object lockObject = new Object();
    private final ReentrantLock reentrantLock = new ReentrantLock();

    /**
     * Базовый инкремент без синхронизации.
     * Демонстрирует проблему race condition в многопоточной среде.
     */
    public void incrementBasic() {
        basicCount++;
    }

    /**
     * Инкремент с использованием синхронизированного метода.
     * Синхронизация осуществляется на объекте this.
     */
    public synchronized void incrementSynchronizedMethod() {
        syncMethodCount++;
    }

    /**
     * Инкремент с использованием синхронизированного блока.
     * Синхронизация осуществляется на специальном объекте-блокировке.
     */
    public void incrementSynchronizedBlock() {
        synchronized (lockObject) {
            syncBlockCount++;
        }
    }

    /**
     * Инкремент с использованием ReentrantLock.
     * Предоставляет более гибкий механизм блокировки по сравнению с synchronized.
     */
    public void incrementReentrantLock() {
        reentrantLock.lock();
        try {
            reentrantCount++;
        } finally {
            reentrantLock.unlock();
        }
    }

    /**
     * Инкремент с использованием AtomicInteger.
     * Использует атомарные операции процессора для обеспечения потокобезопасности.
     */
    public void incrementAtomicInteger() {
        atomicCount.incrementAndGet();
    }

    public static void main(String[] args) throws InterruptedException {
        Counter counter = new Counter();

        Thread[] threads = new Thread[10];
        for (int i = 0; i < 10; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    counter.incrementBasic();
                    counter.incrementSynchronizedMethod();
                    counter.incrementSynchronizedBlock();
                    counter.incrementReentrantLock();
                    counter.incrementAtomicInteger();
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        System.out.println("Ожидаемое значение: 10000");
        System.out.println("Базовый инкремент (race condition): " + counter.getBasicCount());
        System.out.println("Synchronized метод - Результат: " + counter.getSyncMethodCount());
        System.out.println("Synchronized блок - Результат: " + counter.getSyncBlockCount());
        System.out.println("ReentrantLock - Результат: " + counter.getReentrantCount());
        System.out.println("AtomicInteger - Результат: " + counter.getAtomicCount().get());
    }
}