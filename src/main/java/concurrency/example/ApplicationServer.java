package concurrency.example;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ApplicationServer {
    public static void main(String[] args) {
        ExecutorService executors = Executors.newFixedThreadPool(10);

        for (int i = 0; i < 3; i++) {
            MyCallable callable = new MyCallable();
            Future<Long> submit = executors.submit(callable);
        }
        executors.shutdown();
    }
}

class MyCallable implements Callable<Long> {
    @Override
    public Long call() {
        try {
            System.out.println("Started: " + Thread.currentThread().getId());
            Thread.sleep(1000);
            System.out.println("Finished: " + Thread.currentThread().getId());
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
        return Thread.currentThread().getId();
    }
}
