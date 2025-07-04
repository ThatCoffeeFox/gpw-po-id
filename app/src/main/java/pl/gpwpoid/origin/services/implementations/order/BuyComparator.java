package pl.gpwpoid.origin.services.implementations.order;

import java.util.Comparator;

public class BuyComparator implements Comparator<OrderWrapper> {
    @Override
    public int compare(OrderWrapper o1, OrderWrapper o2) {
        int priceCmp;
        if (o1.getOrder().getSharePrice() == null && o2.getOrder().getSharePrice() == null) {
            priceCmp = 0;
        } else if (o1.getOrder().getSharePrice() == null) {
            priceCmp = 1;
        } else if (o2.getOrder().getSharePrice() == null) {
            priceCmp = -1;
        } else {
            priceCmp = o2.getOrder().getSharePrice().compareTo(o1.getOrder().getSharePrice());
        }
        if (priceCmp != 0) return priceCmp;
        return o1.getOrder().getOrderStartDate().compareTo(o2.getOrder().getOrderStartDate());
    }
}
