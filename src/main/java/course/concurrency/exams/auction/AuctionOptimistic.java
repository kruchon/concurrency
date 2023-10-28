package course.concurrency.exams.auction;

import java.util.concurrent.atomic.AtomicReference;

public class AuctionOptimistic implements Auction {

    private Notifier notifier;

    public AuctionOptimistic(Notifier notifier) {
        this.notifier = notifier;
    }

    private AtomicReference<Bid> latestBid = new AtomicReference<>();

    public boolean propose(Bid bid) {
        var shouldUpdatePrice = false;
        Bid value;
        do {
            value = latestBid.get();
            shouldUpdatePrice = (value == null || bid.getPrice() > value.getPrice());
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
}
