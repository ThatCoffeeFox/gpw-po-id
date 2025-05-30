package pl.gpwpoid.origin.services;

import org.springframework.transaction.annotation.Transactional;
import pl.gpwpoid.origin.models.company.Company;
import pl.gpwpoid.origin.models.order.Order;
import pl.gpwpoid.origin.models.order.OrderType;
import pl.gpwpoid.origin.models.wallet.Wallet;
import pl.gpwpoid.origin.ui.views.DTO.OrderDTO;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.util.Collection;
import java.util.Date;


public interface OrderService {

    void addOrder(OrderDTO orderDTO) throws AccessDeniedException;

    void cancelOrder(Order order);

    Collection<Order> getOrders();

    void startOrderMatching(int companyId);

    void stopOrderMatching(int companyId);
}
