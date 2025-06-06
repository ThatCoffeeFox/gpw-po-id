package pl.gpwpoid.origin.services.implementations.order;

import pl.gpwpoid.origin.models.order.Order;

import java.math.BigDecimal;
import java.util.Date;


class OrderWrapper {
    private final Order order;

    private int sharesLeft;
    private BigDecimal shareMatchingPrice;


    OrderWrapper(Order order) {
        this.order = order;
        this.sharesLeft = order.getSharesAmount();
        this.shareMatchingPrice = order.getSharePrice();
    }

    OrderWrapper(Order order, int sharesLeft) {
        this.order = order;
        this.sharesLeft = sharesLeft;
        this.shareMatchingPrice = order.getSharePrice();
    }

    void setShareMatchingPrice(BigDecimal shareMatchingPrice) {
        this.shareMatchingPrice = shareMatchingPrice;
    }

    Order getOrder() {
        return order;
    }

    void tradeShares(int tradeAmount) {
        if (tradeAmount > sharesLeft)
            throw new IllegalArgumentException("Cannot trade more shares that left in the order");
        sharesLeft -= tradeAmount;
    }

    boolean isValid() {
        System.out.println();
        return sharesLeft > 0 && (order.getCancellations() == null || order.getCancellations().isEmpty()) && (order.getOrderExpirationDate() == null || order.getOrderExpirationDate().compareTo(new Date()) >= 0);
    }

    int getSharesLeft() {
        return this.sharesLeft;
    }

    BigDecimal getShareMatchingPrice() {
        return this.shareMatchingPrice;
    }
}

