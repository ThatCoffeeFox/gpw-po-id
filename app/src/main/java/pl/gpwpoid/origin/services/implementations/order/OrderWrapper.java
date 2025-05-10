package pl.gpwpoid.origin.services.implementations.order;

import pl.gpwpoid.origin.models.order.Order;


class OrderWrapper{
    private Order order;

    private int shares_left;

    OrderWrapper(Order order){
        this.order = order;
        this.shares_left = order.getSharesAmount();
    }

    Order getOrder(){
        return order;
    }

    void tradeShares(int tradeAamount){
        if(tradeAamount < shares_left) throw new IllegalArgumentException("Cannot trade more shares that left in the order");
        shares_left -= tradeAamount;
    }
}

