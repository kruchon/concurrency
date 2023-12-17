package course.concurrency.m3_shared.immutable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class OrderService {

    private Map<Long, Order> currentOrders = new ConcurrentHashMap<>();
    private AtomicLong nextId = new AtomicLong(0);

    private long nextId() {
        return nextId.incrementAndGet();
    }

    public long createOrder(List<Item> items) {
        long id = nextId();
        Order order = new Order(id, items);
        currentOrders.put(id, order);
        return id;
    }

    public void updatePaymentInfo(long orderId, PaymentInfo paymentInfo) {
        Order updatedOrder = currentOrders.compute(orderId, (id, order) -> Objects.requireNonNull(order).withPaymentInfo(paymentInfo));
        if (updatedOrder.checkStatus()) {
            deliver(updatedOrder);
        }
    }

    public void setPacked(long orderId) {
        Order updatedOrder = currentOrders.compute(orderId, (id, order) -> Objects.requireNonNull(order).packed());
        if (updatedOrder.checkStatus()) {
            deliver(updatedOrder);
        }
    }

    private void deliver(Order orderToDeliver) {
        /* ... */
        currentOrders.put(orderToDeliver.getId(), orderToDeliver.delivered());
    }

    public boolean isDelivered(long orderId) {
        return currentOrders.get(orderId).getStatus().equals(Order.Status.DELIVERED);
    }
}
