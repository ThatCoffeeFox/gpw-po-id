package pl.gpwpoid.origin.services.implementations.order;

import pl.gpwpoid.origin.models.order.Order;

import java.util.Date;


class OrderWrapper{
    private Order order;

    private int sharesLeft;

    OrderWrapper(Order order){
        this.order = order;
        this.sharesLeft = order.getSharesAmount();
    }

    Order getOrder(){
        return order;
    }

    void tradeShares(int tradeAamount){
        if(tradeAamount > sharesLeft) throw new IllegalArgumentException("Cannot trade more shares that left in the order");
        sharesLeft -= tradeAamount;
    }

    boolean isValid(){
        return order.getCancellations().isEmpty() && order.getOrderExpirationDate().compareTo(new Date()) >= 0 && sharesLeft > 0;
    }

    int getSharesLeft(){
        return this.sharesLeft;
    }
}

