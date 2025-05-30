package pl.gpwpoid.origin.services;

import pl.gpwpoid.origin.models.order.Order;
import pl.gpwpoid.origin.repositories.views.ActiveOrderListItem;
import pl.gpwpoid.origin.ui.views.DTO.OrderDTO;

import java.nio.file.AccessDeniedException;
import java.util.Collection;
import java.util.List;


public interface OrderService {

    void addOrder(OrderDTO orderDTO) throws AccessDeniedException;

    void cancelOrder(Integer orderId);

    List<ActiveOrderListItem> getActiveOrderListItemsForLoggedInAccount();

    Collection<Order> getOrders();

    void startOrderMatching(int companyId);

    void stopOrderMatching(int companyId);
}
