package course.concurrency.m3_shared;

public class PingPong {

    private static final Object pingPongLock = new Object();
    private static boolean isPing = true;

    public static void ping() {
        while (true) {
            synchronized (pingPongLock) {
                while (!isPing) {
                    try {
                        pingPongLock.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                System.out.println("Ping");
                isPing = false;
                pingPongLock.notify();
            }
        }
    }

    public static void pong() {
        while (true) {
            synchronized (pingPongLock) {
                while (isPing) {
                    try {
                        pingPongLock.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                System.out.println("Pong");
                isPing = true;
                pingPongLock.notify();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(() -> ping());
        Thread t2 = new Thread(() -> pong());
        t1.start();
        t2.start();
        Thread.sleep(100000L);
    }
}
