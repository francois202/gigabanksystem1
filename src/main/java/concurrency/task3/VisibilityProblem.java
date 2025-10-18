package concurrency.task3;

public class VisibilityProblem {
    private static volatile boolean flag = true;

    public static void main(String[] args) throws InterruptedException {
        Thread writerThread = new Thread(() -> {
            System.out.println("Writer started");
            sleep(1000);
            flag = false;  // Изменение флага
            System.out.println("Flag set to false");
        });

        Thread readerThread = new Thread(() -> {
            System.out.println("Reader started");
            while (flag) {
            }
            System.out.println("Reader finished");
        });

        writerThread.start();
        readerThread.start();

        writerThread.join();
        readerThread.join();
    }

    private static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
