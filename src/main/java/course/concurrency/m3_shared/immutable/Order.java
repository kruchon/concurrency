package course.concurrency.m3_shared.immutable;

import java.util.Collections;
import java.util.List;

import static course.concurrency.m3_shared.immutable.Order.Status.NEW;

public class Order {
    public enum Status { NEW, IN_PROGRESS, DELIVERED }

    private final Long id;
    private final List<Item> items;
    private final PaymentInfo paymentInfo;
    private final boolean isPacked;
    private final Status status;

    public Order(Long id, List<Item> items) {
        this.id = id;
        this.items = Collections.unmodifiableList(items);
        this.status = NEW;
        this.paymentInfo = null;
        this.isPacked = false;
    }

    public Order(Long id, List<Item> items, Status status, PaymentInfo paymentInfo, boolean isPacked) {
        this.id = id;
        this.items = Collections.unmodifiableList(items);
        this.paymentInfo = paymentInfo;
        this.isPacked = isPacked;
        this.status = status;
    }

    public boolean checkStatus() {
        if (items != null && !items.isEmpty() && paymentInfo != null && isPacked) {
            return true;
        }
        return false;
    }

    public Long getId() {
        return id;
    }

    public List<Item> getItems() {
        return items;
    }

    public PaymentInfo getPaymentInfo() {
        return paymentInfo;
    }

    public boolean isPacked() {
        return isPacked;
    }

    public Status getStatus() {
        return status;
    }

    public Order withPaymentInfo(PaymentInfo paymentInfo) {
        return new Order(this.id, this.items, Status.IN_PROGRESS, paymentInfo, this.isPacked);
    }

    public Order packed() {
        return new Order(this.id, this.items, Status.IN_PROGRESS, this.paymentInfo, true);
    }

    public Order delivered() {
        return new Order(this.id, this.items, Status.DELIVERED, this.paymentInfo, this.isPacked);
    }
}
