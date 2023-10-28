package course.concurrency.exams.auction;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class AuctionPessimistic implements Auction {

    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private Notifier notifier;

    public AuctionPessimistic(Notifier notifier) {
        this.notifier = notifier;
    }

    private volatile Bid latestBid;

    public boolean propose(Bid bid) {
        boolean updated = false;
        var writeLock = lock.writeLock();
        writeLock.lock();
        try {
            if (latestBid == null || bid.getPrice() > latestBid.getPrice()) {
                latestBid = bid;
                updated = true;
            }
        } finally {
            writeLock.unlock();
        }
        if (updated) {
            notifier.sendOutdatedMessage(bid);
        }
        return updated;
    }

    public Bid getLatestBid() {
        var readLock = lock.readLock();
        readLock.lock();
        try {
            return latestBid;
        } finally {
            readLock.unlock();
        }
    }
}
