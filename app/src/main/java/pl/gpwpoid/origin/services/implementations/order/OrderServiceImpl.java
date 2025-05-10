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

    static final Map<Integer, BlockingQueue<Order>> companyIdOrderQueue  = new ConcurrentHashMap<>();

    private final ExecutorService executorService;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository,
                            OrderFactory orderFactory,
                            OrderCancellationFactory orderCancellationFactory,
                            CompanyService companyService,
                            ExecutorService executorService){
        this.orderRepository = orderRepository;

        this.orderFactory = orderFactory;
        this.orderCancellationFactory = orderCancellationFactory;

        this.companyService = companyService;
        this.executorService = executorService;

        for(int id : companyService.getTradableCompaniesId()){
            companyIdOrderQueue.put(id, new LinkedBlockingQueue<>());
            executorService.execute(new OrderMatcher(id));
        }
    }

    @Override
    public void addOrder(OrderType orderType,
                         int shares_amount,
                         BigDecimal sharePrice,
                         Wallet wallet,
                         Company company,
                         Date orderExpirationDate) {
        try{
            Order order = orderFactory.createOrder(orderType,shares_amount,sharePrice,wallet,company,orderExpirationDate);
            orderRepository.save(order);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create order",e);
        }


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

    public void isOrderCanceled(Order order){

    }

    @Override
    public Collection<Order> getOrders() {
        return orderRepository.findAll();
    }
}
