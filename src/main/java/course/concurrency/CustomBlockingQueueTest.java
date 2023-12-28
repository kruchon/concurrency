package course.concurrency;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CustomBlockingQueueTest {

    @Test
    public void putOneAndRemove() {
        CustomBlockingQueue<Integer> queue = new CustomBlockingQueue<>(1);
        queue.enqueue(1);
        Integer dequeuedElement = queue.dequeue();
        assertEquals(1, dequeuedElement);
    }

    @Test
    public void put10000AndRemoveConcurrently() {
        CustomBlockingQueue<Integer> queue = new CustomBlockingQueue<>(10);
        Set<Integer> inputIntegers = new HashSet<>();
        for (int i = 0; i < 10000; i++) {
            inputIntegers.add(i);
        }
        var readingPool = Executors.newFixedThreadPool(10);
        var writingPool = Executors.newFixedThreadPool(10);

        var writingFutures = inputIntegers.stream().map(i ->
            CompletableFuture.runAsync(() -> queue.enqueue(i), writingPool)
        ).toArray(CompletableFuture[]::new);

        var readIntegers = ConcurrentHashMap.newKeySet();
        var readingFutures = inputIntegers.stream().map(i ->
            CompletableFuture.runAsync((() -> readIntegers.add(queue.dequeue())), readingPool)
        ).toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(writingFutures).orTimeout(1, TimeUnit.MINUTES).join();
        CompletableFuture.allOf(readingFutures).orTimeout(1, TimeUnit.MINUTES).join();
        assertEquals(inputIntegers, new HashSet<>(readIntegers));
    }


    @Test
    public void put10000AndOnlyThenRemoveConcurrently() throws InterruptedException {
        CustomBlockingQueue<Integer> queue = new CustomBlockingQueue<>(10);
        Set<Integer> inputIntegers = new HashSet<>();
        for (int i = 0; i < 10000; i++) {
            inputIntegers.add(i);
        }
        var readingPool = Executors.newFixedThreadPool(10);
        var writingPool = Executors.newFixedThreadPool(10);

        inputIntegers.stream().map(i ->
                CompletableFuture.runAsync(() -> queue.enqueue(i), writingPool)
        ).toArray(CompletableFuture[]::new);
        Thread.sleep(1000L);

        var readIntegers = ConcurrentHashMap.newKeySet();
        var readingFutures = inputIntegers.stream().map(i ->
                CompletableFuture.runAsync((() -> readIntegers.add(queue.dequeue())), readingPool)
        ).toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(readingFutures).orTimeout(1, TimeUnit.MINUTES).join();
        assertEquals(inputIntegers, new HashSet<>(readIntegers));
    }

    @Test
    public void remove10000AndOnlyThenPutConcurrently() throws InterruptedException {
        CustomBlockingQueue<Integer> queue = new CustomBlockingQueue<>(10);
        Set<Integer> inputIntegers = new HashSet<>();
        for (int i = 0; i < 10000; i++) {
            inputIntegers.add(i);
        }
        var readingPool = Executors.newFixedThreadPool(10);
        var writingPool = Executors.newFixedThreadPool(10);

        var readIntegers = ConcurrentHashMap.newKeySet();
        var readingFutures = inputIntegers.stream().map(i ->
                CompletableFuture.runAsync((() -> readIntegers.add(queue.dequeue())), readingPool)
        ).toArray(CompletableFuture[]::new);
        Thread.sleep(1000L);

        inputIntegers.stream().map(i ->
                CompletableFuture.runAsync(() -> queue.enqueue(i), writingPool)
        ).toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(readingFutures).orTimeout(1, TimeUnit.MINUTES).join();
        assertEquals(inputIntegers, new HashSet<>(readIntegers));
    }
}
