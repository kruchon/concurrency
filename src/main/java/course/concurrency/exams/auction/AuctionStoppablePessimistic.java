package course.concurrency.exams.auction;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class AuctionStoppablePessimistic implements AuctionStoppable {

    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private Notifier notifier;
    private volatile boolean isAuctionFinished = false;

    public AuctionStoppablePessimistic(Notifier notifier) {
        this.notifier = notifier;
    }

    private Bid latestBid;

    public boolean propose(Bid bid) {
        boolean updated = false;
        if (!isAuctionFinished && (latestBid == null || bid.getPrice() > latestBid.getPrice())) {
            var writeLock = lock.writeLock();
            writeLock.lock();
            try {
                if (!isAuctionFinished && (latestBid == null || bid.getPrice() > latestBid.getPrice())) {
                    latestBid = bid;
                    updated = true;
                }
            } finally {
                writeLock.unlock();
            }
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

    @Override
    public Bid stopAuction() {
        isAuctionFinished = true;
        var readLock = lock.readLock();
        readLock.lock();
        try {
           return latestBid;
        } finally {
            readLock.unlock();
        }
    }
}
