package pl.gpwpoid.origin.services.implementations.order;

import org.checkerframework.checker.units.qual.Acceleration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.gpwpoid.origin.factories.OrderFactory;
import pl.gpwpoid.origin.models.order.Order;
import pl.gpwpoid.origin.models.order.OrderType;
import pl.gpwpoid.origin.repositories.projections.ActiveOrderProjection;
import pl.gpwpoid.origin.services.WalletsService;

@Component
public class OrderWrapperFactory {
    private final WalletsService walletsService;
    private final OrderFactory orderFactory;

    private final OrderType buyOrderType, sellOrderType;

    @Autowired
    OrderWrapperFactory(WalletsService walletsService, OrderFactory orderFactory){
        this.walletsService = walletsService;
        this.orderFactory = orderFactory;
        buyOrderType = new OrderType();
        sellOrderType = new OrderType();
        buyOrderType.setOrderType("buy");
        sellOrderType.setOrderType("sell");
    }

    OrderWrapper createOrderWrapper(Order order) {
        OrderWrapper orderWrapper = new OrderWrapper(order);
        if (order.getOrderType().getOrderType().equals("buy") && order.getSharePrice() == null) {
            orderWrapper.setShareMatchingPrice(walletsService.getWalletUnblockedFoundsBeforeMarketBuyOrder(order.getOrderId()));
        }
        return orderWrapper;
    }

    OrderWrapper createBuyOrderWrapper(ActiveOrderProjection activeOrderProjection){
        Order order = orderFactory.createOrder(activeOrderProjection, buyOrderType);
        return createOrderWrapper(order);
    }

    OrderWrapper createSellOrderWrapper(ActiveOrderProjection activeOrderProjection){
        Order order = orderFactory.createOrder(activeOrderProjection, sellOrderType);
        return createOrderWrapper(order);
    }
}
