package pl.gpwpoid.origin.services;

import org.springframework.transaction.annotation.Transactional;
import pl.gpwpoid.origin.models.company.Company;
import pl.gpwpoid.origin.models.order.Order;
import pl.gpwpoid.origin.models.order.OrderType;
import pl.gpwpoid.origin.models.wallet.Wallet;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;


public interface OrderService {
    void addOrder(OrderType orderType,
                  int shares_amount,
                  BigDecimal sharePrice,
                  Wallet wallet,
                  Company company,
                  Date orderExpirationDate);

    void cancelOrder(Order order);

    Collection<Order> getOrders();

    @Transactional
    void startOrderMatching(int companyId);

    @Transactional
    void stopOrderMatching(int companyId);
}
