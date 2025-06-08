package pl.gpwpoid.origin.services;

import com.vaadin.flow.data.provider.DataProvider;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import pl.gpwpoid.origin.models.order.Order;
import pl.gpwpoid.origin.repositories.DTO.ActiveOrderDTO;
import pl.gpwpoid.origin.repositories.views.ActiveOrderListItem;
import pl.gpwpoid.origin.ui.views.DTO.OrderDTO;

import java.nio.file.AccessDeniedException;
import java.util.Collection;
import java.util.List;


public interface OrderService {

    void addOrder(OrderDTO orderDTO) throws AccessDeniedException;

    @Transactional
    void cancelOrder(Integer orderId);

    List<ActiveOrderListItem> getActiveOrderListItemsForLoggedInAccount();
    List<ActiveOrderListItem> getOrderListItemsByAccountId(Integer accountId, Pageable pageable);

    Collection<Order> getOrders();

    void startOrderMatching(int companyId);

    void stopOrderMatching(int companyId);

    List<ActiveOrderDTO> getActiveOrderDTOListByWalletIdCompanyId(Integer walletId, Integer companyId) throws AccessDeniedException;

    Boolean isCanceledOrder(Integer orderId);
}

