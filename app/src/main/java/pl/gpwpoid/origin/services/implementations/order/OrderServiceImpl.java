package pl.gpwpoid.origin.services.implementations.order;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import pl.gpwpoid.origin.factories.OrderCancellationFactory;
import pl.gpwpoid.origin.factories.OrderFactory;
import pl.gpwpoid.origin.models.company.Company;
import pl.gpwpoid.origin.models.order.Order;
import pl.gpwpoid.origin.models.order.OrderCancellation;
import pl.gpwpoid.origin.models.order.OrderType;
import pl.gpwpoid.origin.models.wallet.Wallet;
import pl.gpwpoid.origin.repositories.OrderRepository;
import pl.gpwpoid.origin.repositories.projections.ActiveOrderProjection;
import pl.gpwpoid.origin.services.CompanyService;
import pl.gpwpoid.origin.services.OrderService;
import pl.gpwpoid.origin.services.TransactionService;
import pl.gpwpoid.origin.services.WalletsService;
import pl.gpwpoid.origin.ui.views.DTO.OrderDTO;
import pl.gpwpoid.origin.utils.SecurityUtils;

import java.lang.Integer;
import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.*;

@Service
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;

    private final OrderFactory orderFactory;
    private final OrderCancellationFactory orderCancellationFactory;
    private final OrderWrapperFactory orderWrapperFactory;

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
                            OrderWrapperFactory orderWrapperFactory,
                            CompanyService companyService,
                            TransactionService transactionService,
                            ConcurrentMap<Integer,BlockingQueue<Order>> companyIdOrderQueue,
                            @Qualifier("orderExecutorService") ExecutorService orderExecutorService,
                            WalletsService walletsService){
        this.orderRepository = orderRepository;

        this.orderFactory = orderFactory;
        this.orderCancellationFactory = orderCancellationFactory;
        this.orderWrapperFactory = orderWrapperFactory;

        this.companyService = companyService;
        this.transactionService = transactionService;
        this.companyIdOrderQueue = companyIdOrderQueue;
        this.orderExecutorService = orderExecutorService;
        this.walletsService = walletsService;

        for(int id : this.companyService.getTradableCompaniesId()){
           startOrderMatching(id);
        }
    }

    private boolean hasEnoughFoundsOrShares(OrderDTO orderDTO){
        if(orderDTO.getOrderType().equals("buy")){
            BigDecimal foundsInWallet = walletsService.getWalletUnblockedFundsById(orderDTO.getWalletId());
            if(orderDTO.getSharePrice() == null) {
                return foundsInWallet.compareTo(BigDecimal.ZERO) > 0;
            }
            BigDecimal foundsNeeded = orderDTO.getSharePrice().multiply(BigDecimal.valueOf(orderDTO.getSharesAmount()));
            return foundsInWallet.compareTo(foundsNeeded) >= 0;
        } else if (orderDTO.getOrderType().equals("sell")) {
            Integer sharesInWallet = walletsService.getWalletUnblockedSharesAmount(orderDTO.getWalletId(), orderDTO.getCompanyId());
            return sharesInWallet.compareTo(orderDTO.getSharesAmount()) >= 0;
        }
        return false;
    };


    @Override
    @Transactional
    public void addOrder(OrderDTO orderDTO) throws AccessDeniedException {
        Optional<Company> company = companyService.getCompanyById(orderDTO.getCompanyId());
        if(company.isEmpty()) throw new EntityNotFoundException("This company does not exist");

        Optional<Wallet> wallet = walletsService.getWalletById(orderDTO.getWalletId());

        if(wallet.isEmpty()) throw new EntityNotFoundException("This wallet does not exist");
        if (!wallet.get().getAccount().getAccountId().equals(SecurityUtils.getAuthenticatedAccountId())){
            throw new AccessDeniedException("You are not an owner of the wallet");
        }
        if(!hasEnoughFoundsOrShares(orderDTO)){
            throw new RuntimeException("You don't have enough shares/founds");
        }



        Order order = orderFactory.createOrder(orderDTO, wallet.get(), company.get());

        try {
            orderRepository.save(order);
            orderRepository.flush();
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to create order", e);
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                companyIdOrderQueue
                        .get(order.getCompany().getCompanyId())
                        .add(order);
            }
        });
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
                orderExecutorService.submit(
                        new OrderMatchingWorker(
                                id,
                                getActiveBuyOrderWrappersByCompanyId(companyId),
                                getActiveSellOrderWrappersByCompanyId(companyId),
                                transactionService.getShareValueByCompanyId(companyId),
                                transactionService,
                                orderWrapperFactory,
                                companyIdOrderQueue))
        );
    }

    @Transactional
    @Override
    public void stopOrderMatching(int companyId) {
        Future<?> future = companyOrderMatcherFutures.get(companyId);
        if (future != null && !future.isDone() && !future.isCancelled()) {
            future.cancel(true);
        }
        companyOrderMatcherFutures.remove(companyId);
    }


    private List<OrderWrapper> getActiveBuyOrderWrappersByCompanyId(Integer companyId) {
        List<ActiveOrderProjection> projections = orderRepository.findActiveBuyOrdersByCompanyId(companyId);
        OrderType orderType = new OrderType();
        orderType.setOrderType("buy");
        Optional<Company> company = companyService.getCompanyById(companyId);
        return projections.stream().map(orderWrapperFactory::createBuyOrderWrapper).toList();
    }


    private List<OrderWrapper> getActiveSellOrderWrappersByCompanyId(Integer companyId) {
        List<ActiveOrderProjection> projections = orderRepository.findActiveSellOrdersByCompanyId(companyId);
        OrderType orderType = new OrderType();
        orderType.setOrderType("sell");
        Optional<Company> company = companyService.getCompanyById(companyId);
        return projections.stream().map(orderWrapperFactory::createSellOrderWrapper).toList();
    }
}
