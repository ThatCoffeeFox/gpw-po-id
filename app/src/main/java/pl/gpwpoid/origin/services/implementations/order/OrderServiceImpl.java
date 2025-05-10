package pl.gpwpoid.origin.services.implementations.order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.gpwpoid.origin.factories.OrderCancellationFactory;
import pl.gpwpoid.origin.factories.OrderFactory;
import pl.gpwpoid.origin.models.company.Company;
import pl.gpwpoid.origin.models.order.Order;
import pl.gpwpoid.origin.models.order.OrderCancellation;
import pl.gpwpoid.origin.models.order.OrderType;
import pl.gpwpoid.origin.models.wallet.Wallet;
import pl.gpwpoid.origin.repositories.OrderRepository;
import pl.gpwpoid.origin.services.CompanyService;
import pl.gpwpoid.origin.services.OrderService;
import pl.gpwpoid.origin.services.TransactionService;

import java.lang.Integer;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.*;

@Service
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;

    private final OrderFactory orderFactory;
    private final OrderCancellationFactory orderCancellationFactory;

    private final CompanyService companyService;
    private final TransactionService transactionService;

    private final Map<Integer, BlockingQueue<Order>> companyIdOrderQueue;
    private final Map<Integer, Future<?>> companyOrderMatcherFutures = new ConcurrentHashMap<>();

    private final ExecutorService orderExecutorService;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository,
                            OrderFactory orderFactory,
                            OrderCancellationFactory orderCancellationFactory,
                            CompanyService companyService,
                            TransactionService transactionService,
                            Map<Integer,BlockingQueue<Order>> companyIdOrderQueue,
                            ExecutorService orderExecutorService){
        this.orderRepository = orderRepository;

        this.orderFactory = orderFactory;
        this.orderCancellationFactory = orderCancellationFactory;

        this.companyService = companyService;
        this.transactionService = transactionService;
        this.companyIdOrderQueue = companyIdOrderQueue;
        this.orderExecutorService = orderExecutorService;

        for(int id : companyService.getTradableCompaniesId()){
           startOrderMatching(id);
        }
    }

    @Override
    public void addOrder(OrderType orderType,
                         int shares_amount,
                         BigDecimal sharePrice,
                         Wallet wallet,
                         Company company,
                         Date orderExpirationDate) {
        Order order;
        try{
            order = orderFactory.createOrder(orderType,shares_amount,sharePrice,wallet,company,orderExpirationDate);
            orderRepository.save(order);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create order",e);
        }

        companyIdOrderQueue.get(company.getCompanyId()).add(order);
    }

    @Override
    public void cancelOrder(Order order) {
        try{
            OrderCancellation newOrderCancellation = orderCancellationFactory.createOrderCancellation(order);
            orderRepository.save(order);
        }
        catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public Collection<Order> getOrders() {
        return orderRepository.findAll();
    }

    public void startOrderMatching(int companyId) {
        companyIdOrderQueue.putIfAbsent(companyId, new LinkedBlockingQueue<>());

        companyOrderMatcherFutures.computeIfAbsent(companyId, id ->
                orderExecutorService.submit(new OrderMatchingWorker(id, transactionService, companyIdOrderQueue))
        );
    }

    public void stopOrderMatching(int companyId) {
        Future<?> future = companyOrderMatcherFutures.get(companyId);
        if (future != null && !future.isDone() && !future.isCancelled()) {
            future.cancel(true); // Interrupts the thread
        }
        companyOrderMatcherFutures.remove(companyId);
    }

}
