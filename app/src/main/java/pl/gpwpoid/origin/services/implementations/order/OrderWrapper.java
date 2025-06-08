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

    Order getOrder() {
        return order;
    }

    void tradeShares(int tradeAmount) {
        if (tradeAmount > sharesLeft)
            throw new IllegalArgumentException("Cannot trade more shares that left in the order");
        sharesLeft -= tradeAmount;
    }

    Boolean isExpiredOrEmpty() {
        Date expiration = order.getOrderExpirationDate();
        Date now = new Date();
        return sharesLeft <= 0 || (expiration != null && !expiration.after(now));
    }

    int getSharesLeft() {
        return this.sharesLeft;
    }

    BigDecimal getShareMatchingPrice() {
        return this.shareMatchingPrice;
    }

    void setShareMatchingPrice(BigDecimal shareMatchingPrice) {
        this.shareMatchingPrice = shareMatchingPrice;
    }
}

