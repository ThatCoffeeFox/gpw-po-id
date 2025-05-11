package pl.gpwpoid.origin.services.implementations.order;


import java.math.BigDecimal;
import java.util.Comparator;

public class SellComparator implements Comparator<OrderWrapper> {
    @Override
    public int compare(OrderWrapper o1, OrderWrapper o2) {
        int priceCmp = o1.getOrder().getSharePrice().compareTo(o2.getOrder().getSharePrice());
        if (priceCmp != 0) return priceCmp;

        return o1.getOrder().getOrderStartDate().compareTo(o2.getOrder().getOrderStartDate());
    }
}
