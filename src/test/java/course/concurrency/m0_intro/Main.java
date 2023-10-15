package course.concurrency.m0_intro;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Main {
    public static long longTask() throws InterruptedException {
        Thread.sleep(10000); // + try-catch
        return ThreadLocalRandom.current().nextInt();
    }

    public static void main(String[] args) {

        ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 10, 60, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());

        for (int i = 0; i < 10; i++) {
            executor.submit(() -> longTask());
            System.out.print(executor.getPoolSize() + " ");
        }

        executor.shutdown();
    }
}
