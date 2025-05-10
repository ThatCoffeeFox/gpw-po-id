package pl.gpwpoid.origin.services.implementations.order;

import java.math.BigDecimal;
import java.util.Comparator;

public class BuyComparator implements Comparator<OrderWrapper> {
    @Override
    public int compare(OrderWrapper o1, OrderWrapper o2) {
        // Compare prices (descending order, bigger price should come first)
        int priceCmp = o2.getOrder().getSharePrice().compareTo(o1.getOrder().getSharePrice());
        if (priceCmp != 0) return priceCmp;

        // If prices are equal, compare by order start date (ascending order)
        return o1.getOrder().getOrderStartDate().compareTo(o2.getOrder().getOrderStartDate());
    }
}
