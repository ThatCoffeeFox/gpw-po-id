package pl.gpwpoid.origin.services.implementations.order;

import org.springframework.transaction.annotation.Transactional;
import pl.gpwpoid.origin.models.order.Order;
import pl.gpwpoid.origin.services.OrderService;
import pl.gpwpoid.origin.services.TransactionService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;

public class OrderMatchingWorker implements Runnable {
    private final int companyId;
    private final PriorityQueue<OrderWrapper> buyQueue;
    private final PriorityQueue<OrderWrapper> sellQueue;
    private final TransactionService transactionService;
    private final Map<Integer, BlockingQueue<Order>> companyIdOrderQueue;

    private final OrderWrapperFactory orderWrapperFactory;
    private final OrderService orderService;

    private BigDecimal recentTransactionSharePrice;

    OrderMatchingWorker(int companyId,
                        List<OrderWrapper> activeBuyOrders,
                        List<OrderWrapper> activeSellOrders,
                        BigDecimal recentTransactionSharePrice,
                        TransactionService transactionService,
                        OrderWrapperFactory orderWrapperFactory,
                        ConcurrentMap<Integer, BlockingQueue<Order>> companyIdOrderQueue, OrderService orderService) {
        this.companyId = companyId;
        this.orderService = orderService;
        this.buyQueue = new PriorityQueue<>(new BuyComparator());
        this.sellQueue = new PriorityQueue<>(new SellComparator());
        this.recentTransactionSharePrice = recentTransactionSharePrice;
        this.transactionService = transactionService;
        this.orderWrapperFactory = orderWrapperFactory;
        this.companyIdOrderQueue = companyIdOrderQueue;

        if (activeBuyOrders != null) this.buyQueue.addAll(activeBuyOrders);
        if (activeSellOrders != null) this.sellQueue.addAll(activeSellOrders);
    }

    private Boolean canMatchOrders(OrderWrapper buyOrder, OrderWrapper sellOrder) {
        return sellOrder.getOrder().getSharePrice() == null ||
                (sellOrder.getOrder().getSharePrice().compareTo(buyOrder.getShareMatchingPrice()) <= 0);
    }

    private BigDecimal getSharePrice(OrderWrapper buyOrder, OrderWrapper sellOrder) {
        if (buyOrder.getOrder().getSharePrice() == null && sellOrder.getOrder().getSharePrice() == null) {
            return this.recentTransactionSharePrice;
        } else if (buyOrder.getOrder().getSharePrice() == null) {
            return sellOrder.getOrder().getSharePrice();
        } else if (sellOrder.getOrder().getSharePrice() == null) {
            return buyOrder.getOrder().getSharePrice();
        } else if (buyOrder.getOrder().getOrderStartDate().compareTo(sellOrder.getOrder().getOrderStartDate()) < 0) {
            return buyOrder.getOrder().getSharePrice();
        } else {
            return sellOrder.getOrder().getSharePrice();
        }
    }

    @Transactional
    private boolean match(){
        OrderWrapper buyOrder = buyQueue.peek(), sellOrder = sellQueue.peek();
        if (buyOrder.isExpiredOrEmpty() || orderService.isCanceledOrder(buyOrder.getOrder().getOrderId())) {
            buyQueue.poll();
            return true;
        }
        if (sellOrder.isExpiredOrEmpty() || orderService.isCanceledOrder(sellOrder.getOrder().getOrderId())) {
            sellQueue.poll();
            return true;
        }
        if (!canMatchOrders(buyOrder, sellOrder)) return false;
        BigDecimal sharePrice = getSharePrice(buyOrder, sellOrder);
        int sharesAmount = Math.min(buyOrder.getSharesLeft(), sellOrder.getSharesLeft());
        try {
            transactionService.addTransaction(sellOrder.getOrder(), buyOrder.getOrder(), sharesAmount, sharePrice);
            buyOrder.tradeShares(sharesAmount);
            sellOrder.tradeShares(sharesAmount);
            recentTransactionSharePrice = sharePrice;
        } catch (Exception e) {
            throw new RuntimeException("Transaction failed", e);
        }
        return true;
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            while (!(buyQueue.isEmpty() || sellQueue.isEmpty())) {//matching loop
                if(!match())break;
            }
            try {
                Order order = companyIdOrderQueue.get(companyId).take();
                if ("sell".equals(order.getOrderType().getOrderType())) {
                    sellQueue.add(orderWrapperFactory.createOrderWrapper(order));
                }
                if ("buy".equals(order.getOrderType().getOrderType())) {
                    buyQueue.add(orderWrapperFactory.createOrderWrapper(order));
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
