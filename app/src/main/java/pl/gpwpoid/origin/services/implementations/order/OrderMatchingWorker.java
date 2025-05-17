package pl.gpwpoid.origin.services.implementations.order;

import org.springframework.transaction.annotation.Transactional;
import pl.gpwpoid.origin.models.order.Order;
import pl.gpwpoid.origin.services.TransactionService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.BlockingQueue;

public class OrderMatchingWorker implements Runnable {
    private final int companyId;
    private final PriorityQueue<OrderWrapper> buyQueue;
    private final PriorityQueue<OrderWrapper> sellQueue;
    private final TransactionService transactionService;
    private final Map<Integer, BlockingQueue<Order>> companyIdOrderQueue;
    private BigDecimal recentTransactionSharePrice;

    OrderMatchingWorker(int companyId,
                        List<OrderWrapper> activeBuyOrders,
                        List<OrderWrapper> activeSellOrders,
                        BigDecimal recentTransactionSharePrice,
                        TransactionService transactionService,
                        Map<Integer, BlockingQueue<Order>> companyIdOrderQueue){
        this.companyId = companyId;
        this.buyQueue = new PriorityQueue<>(new BuyComparator() );
        this.sellQueue = new PriorityQueue<>(new SellComparator());
        this.recentTransactionSharePrice = recentTransactionSharePrice;
        this.transactionService = transactionService;
        this.companyIdOrderQueue = companyIdOrderQueue;

        if(activeBuyOrders != null) this.buyQueue.addAll(activeBuyOrders);
        if(activeSellOrders != null) this.sellQueue.addAll(activeSellOrders);
    }
    @Override
    public void run() {
        try{
            tryMatchOrders();
            while(!Thread.interrupted()){
                Order order = companyIdOrderQueue.get(companyId).take();
                if("sell".equals(order.getOrderType().getOrderType())){
                    sellQueue.add(new OrderWrapper(order));
                }
                if("buy".equals(order.getOrderType().getOrderType())){
                    buyQueue.add(new OrderWrapper(order));
                }
                tryMatchOrders();
            }
        }
        catch (InterruptedException e){
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            throw new RuntimeException("Failed in matching worker", e);
        }
    }

    void tryMatchOrders(){
        if(buyQueue.isEmpty() || sellQueue.isEmpty())return;
        OrderWrapper buyOrder =  buyQueue.peek(), sellOrder = sellQueue.peek();
        System.out.println(buyOrder.isValid());
        if(!buyOrder.isValid()){
            buyQueue.poll();
            tryMatchOrders();
            return;
        }
        System.out.println(sellOrder.isValid());
        if(!sellOrder.isValid()){
            sellQueue.poll();
            tryMatchOrders();
            return;
        }
        if(sellOrder.getOrder().getSharePrice().compareTo(buyOrder.getOrder().getSharePrice()) > 0) return;

        int sharesAmount = Math.min(buyOrder.getSharesLeft(),  sellOrder.getSharesLeft());
        BigDecimal sharePrice;
        if(buyOrder.getOrder().getOrderStartDate().compareTo(sellOrder.getOrder().getOrderStartDate()) > 0)sharePrice = sellOrder.getOrder().getSharePrice();
        else sharePrice = buyOrder.getOrder().getSharePrice();

        try{
            transactionService.addTransaction(sellOrder.getOrder(), buyOrder.getOrder(), sharesAmount, sharePrice);
            buyOrder.tradeShares(sharesAmount);
            sellOrder.tradeShares(sharesAmount);
        }
        catch (Exception e){
            throw new RuntimeException("Transaction failed", e);
        }
        tryMatchOrders();
    }
}
