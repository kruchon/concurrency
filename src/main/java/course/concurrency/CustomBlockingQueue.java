package course.concurrency;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CustomBlockingQueue<T> {

    private final List<T> elements;
    private final Lock lock = new ReentrantLock();
    private final Condition enqueueCondition = lock.newCondition();
    private final Condition dequeueCondition = lock.newCondition();
    private final int maxSize;

    public CustomBlockingQueue(int maxSize) {
        elements = new LinkedList<>();
        this.maxSize = maxSize;
    }

    public void enqueue(T value) {
        try {
            lock.lock();
            while (elements.size() == maxSize) {
                enqueueCondition.awaitUninterruptibly();
            }

            System.out.println("Add " + value);
            elements.addFirst(value);
            dequeueCondition.signal();
        } finally {
            lock.unlock();
        }
    }
    public T dequeue() {
        T last;
        try {
            lock.lock();
            while (elements.isEmpty()) {
                dequeueCondition.awaitUninterruptibly();
            }
            last = elements.removeLast();

            System.out.println("Remove " + last);
            enqueueCondition.signal();
        } finally {
            lock.unlock();
        }
        return last;
    }
}
