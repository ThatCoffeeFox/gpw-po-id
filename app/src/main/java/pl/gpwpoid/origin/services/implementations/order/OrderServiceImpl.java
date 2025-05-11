package pl.gpwpoid.origin.services.implementations.order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import pl.gpwpoid.origin.factories.OrderCancellationFactory;
import pl.gpwpoid.origin.factories.OrderFactory;
import pl.gpwpoid.origin.models.order.Order;
import pl.gpwpoid.origin.models.order.OrderCancellation;
import pl.gpwpoid.origin.models.wallet.Wallet;
import pl.gpwpoid.origin.repositories.OrderRepository;
import pl.gpwpoid.origin.services.CompanyService;
import pl.gpwpoid.origin.services.OrderService;
import pl.gpwpoid.origin.services.TransactionService;
import pl.gpwpoid.origin.services.WalletsService;
import pl.gpwpoid.origin.ui.views.DTO.OrderDTO;

import java.lang.Integer;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;

@Service
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;

    private final OrderFactory orderFactory;
    private final OrderCancellationFactory orderCancellationFactory;

    private final CompanyService companyService;
    private final TransactionService transactionService;
    private final WalletsService walletsService;

    private final ConcurrentMap<Integer, BlockingQueue<Order>> companyIdOrderQueue;
    private final Map<Integer, Future<?>> companyOrderMatcherFutures = new ConcurrentHashMap<>();

    private final ExecutorService orderExecutorService;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository,
                            OrderFactory orderFactory,
                            OrderCancellationFactory orderCancellationFactory,
                            CompanyService companyService,
                            TransactionService transactionService,
                            ConcurrentMap<Integer,BlockingQueue<Order>> companyIdOrderQueue,
                            ExecutorService orderExecutorService,
                            WalletsService walletsService){
        this.orderRepository = orderRepository;

        this.orderFactory = orderFactory;
        this.orderCancellationFactory = orderCancellationFactory;

        this.companyService = companyService;
        this.transactionService = transactionService;
        this.companyIdOrderQueue = companyIdOrderQueue;
        this.orderExecutorService = orderExecutorService;
        this.walletsService = walletsService;

        for(int id : this.companyService.getTradableCompaniesId()){
           startOrderMatching(id);
        }
    }


    @Override
    @Transactional
    public void addOrder(OrderDTO orderDTO) {
        Order order;
        try {
            Optional<Wallet> wallet = walletsService.getWalletById(orderDTO.getWallet().getWalletId());
            if (!wallet.isPresent()) {
                throw new RuntimeException("This wallet does not exist");
            }

            Date orderExpirationDate = null;
            if (orderDTO.getDateTime() != null) {
                ZoneId zonedDateTime = ZoneId.of("UTC");
                orderExpirationDate = Date.from(orderDTO.getDateTime().atZone(zonedDateTime).toInstant());
            }

            order = orderFactory.createOrder(
                    orderDTO.getOrderType(),
                    orderDTO.getAmount(),
                    orderDTO.getPrice(),
                    wallet.get(),
                    orderDTO.getCompany(),
                    orderExpirationDate
            );

            orderRepository.save(order);
            orderRepository.flush();

            Order finalOrder = order;

            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    companyIdOrderQueue
                            .get(orderDTO.getCompany().getCompanyId())
                            .add(finalOrder);
                }
            });

        } catch (Exception e) {
            throw new RuntimeException("Failed to create order", e);
        }
    }


    @Override
    @Transactional
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
    @Transactional
    public Collection<Order> getOrders() {
        return orderRepository.findAll();
    }

    @Transactional
    @Override
    public void startOrderMatching(int companyId) {
        companyIdOrderQueue.putIfAbsent(companyId, new LinkedBlockingQueue<>());

        companyOrderMatcherFutures.computeIfAbsent(companyId, id ->
                orderExecutorService.submit(new OrderMatchingWorker(id, transactionService, companyIdOrderQueue))
        );
    }

    @Transactional
    @Override
    public void stopOrderMatching(int companyId) {
        Future<?> future = companyOrderMatcherFutures.get(companyId);
        if (future != null && !future.isDone() && !future.isCancelled()) {
            future.cancel(true); // Interrupts the thread
        }
        companyOrderMatcherFutures.remove(companyId);
    }

}
