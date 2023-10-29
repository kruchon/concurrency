package course.concurrency.exams.auction;

import java.util.concurrent.atomic.AtomicReference;

public class AuctionStoppableOptimistic implements AuctionStoppable {

    private Notifier notifier;

    private volatile boolean isAuctionFinished = false;

    public AuctionStoppableOptimistic(Notifier notifier) {
        this.notifier = notifier;
    }

    private AtomicReference<Bid> latestBid = new AtomicReference<>();

    public boolean propose(Bid bid) {
        var shouldUpdatePrice = false;
        Bid value;
        do {
            value = latestBid.get();
            shouldUpdatePrice = !isAuctionFinished && (value == null || bid.getPrice() > value.getPrice());
            if (!shouldUpdatePrice) {
                break;
            }
        } while (!latestBid.compareAndSet(value, bid));
        if (shouldUpdatePrice) {
            notifier.sendOutdatedMessage(bid);
        }
        return shouldUpdatePrice;
    }

    public Bid getLatestBid() {
        return latestBid.get();
    }

    @Override
    public Bid stopAuction() {
        isAuctionFinished = true;
        return latestBid.get();
    }
}
