package course.concurrency.m2_async.cf.min_price;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class PriceAggregator {

    private PriceRetriever priceRetriever = new PriceRetriever();

    public void setPriceRetriever(PriceRetriever priceRetriever) {
        this.priceRetriever = priceRetriever;
    }

    private Collection<Long> shopIds = Set.of(10l, 45l, 66l, 345l, 234l, 333l, 67l, 123l, 768l);


    private final Executor executor = Executors.newCachedThreadPool();

    public void setShops(Collection<Long> shopIds) {
        this.shopIds = shopIds;
    }

    public double getMinPrice(long itemId) {
        var shopIdFutures = shopIds.stream()
                .map(
                        shopId -> CompletableFuture.supplyAsync(
                                        () -> Optional.of(priceRetriever.getPrice(itemId, shopId)),
                                        executor
                                ).exceptionally(e -> Optional.empty())
                                .completeOnTimeout(Optional.empty(), 2950, TimeUnit.MILLISECONDS)
                ).collect(Collectors.toList());
        return shopIdFutures
                .stream()
                .map(CompletableFuture::join)
                .filter(Optional::isPresent)
                .mapToDouble(Optional::get)
                .min()
                .orElse(Double.NaN);
    }
}
