package pl.gpwpoid.origin.services.implementations.order;

import pl.gpwpoid.origin.models.order.Order;
import pl.gpwpoid.origin.services.TransactionService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;

public class OrderMatchingWorker implements Runnable {
    private final int companyId;
    private final PriorityQueue<OrderWrapper> buyQueue;
    private final PriorityQueue<OrderWrapper> sellQueue;
    private final TransactionService transactionService;
    private final Map<Integer, BlockingQueue<Order>> companyIdOrderQueue;

    private final OrderWrapperFactory orderWrapperFactory;

    private BigDecimal recentTransactionSharePrice;

    OrderMatchingWorker(int companyId,
                        List<OrderWrapper> activeBuyOrders,
                        List<OrderWrapper> activeSellOrders,
                        BigDecimal recentTransactionSharePrice,
                        TransactionService transactionService,
                        OrderWrapperFactory orderWrapperFactory,
                        ConcurrentMap<Integer, BlockingQueue<Order>> companyIdOrderQueue) {
        this.companyId = companyId;
        this.buyQueue = new PriorityQueue<>(new BuyComparator());
        this.sellQueue = new PriorityQueue<>(new SellComparator());
        this.recentTransactionSharePrice = recentTransactionSharePrice;
        this.transactionService = transactionService;
        this.orderWrapperFactory = orderWrapperFactory;
        this.companyIdOrderQueue = companyIdOrderQueue;

        if (activeBuyOrders != null) this.buyQueue.addAll(activeBuyOrders);
        if (activeSellOrders != null) this.sellQueue.addAll(activeSellOrders);
    }

    private boolean canMatchOrders(OrderWrapper buyOrder, OrderWrapper sellOrder) {
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

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            while (!(buyQueue.isEmpty() || sellQueue.isEmpty())) {//matching loop
                OrderWrapper buyOrder = buyQueue.peek(), sellOrder = sellQueue.peek();
                if (!buyOrder.isValid()) {
                    buyQueue.poll();
                    continue;
                }
                if (!sellOrder.isValid()) {
                    sellQueue.poll();
                    continue;
                }
                if (!canMatchOrders(buyOrder, sellOrder)) break;
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
