package pl.gpwpoid.origin.services.implementations.order;

import pl.gpwpoid.origin.models.order.Order;
import pl.gpwpoid.origin.services.OrderService;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.PriorityQueue;

public class OrderMatcher implements Runnable {
    private final int companyId;
    private final PriorityQueue<OrderWrapper> buyQueue;
    private final PriorityQueue<OrderWrapper> sellQueue;

    OrderMatcher(int companyId){
        this.companyId = companyId;
        this.buyQueue = new PriorityQueue<>(new BuyComparator());
        this.sellQueue = new PriorityQueue<>(new SellComparator());
    }
    @Override
    public void run() {
        try{
            while(!Thread.interrupted()){
                Order order = OrderServiceImpl.companyIdOrderQueue.get(companyId).take();
                if("sell".equals(order.getOrderType().getOrderType())){
                    sellQueue.add(new OrderWrapper(order));
                }
                if("buy".equals(order.getOrderType().getOrderType())){
                    buyQueue.add(new OrderWrapper(order));
                }
                tryMatchOrders();
            }
        }
        catch (Exception e){
            throw new RuntimeException("Order matcher interrupted", e);
        }
    }
    void tryMatchOrders(){
    }
}
