package pl.gpwpoid.origin.factories;

import org.springframework.stereotype.Component;
import pl.gpwpoid.origin.models.company.Company;
import pl.gpwpoid.origin.models.order.Order;
import pl.gpwpoid.origin.models.order.OrderType;
import pl.gpwpoid.origin.models.wallet.Wallet;
import pl.gpwpoid.origin.repositories.projections.ActiveOrderProjection;
import pl.gpwpoid.origin.ui.views.DTO.OrderDTO;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.Date;


@Component
public class OrderFactory {

    void checkOrderConstrains(OrderType orderType,
                              int shares_amount,
                              BigDecimal sharePrice,
                              Wallet wallet,
                              Company company,
                              Date orderStartDate,
                              Date orderExpirationDate) {
        if (orderType == null) {
            throw new IllegalArgumentException("Order type cannot be null");
        }
        if (shares_amount <= 0) {
            throw new IllegalArgumentException("Shares amount has to be grater than zero");
        }
        if (sharePrice != null && sharePrice.compareTo(BigDecimal.valueOf(0)) <= 0) {
            throw new IllegalArgumentException("Share price has to be null or greater than zero");
        }
        if (wallet == null) {
            throw new IllegalArgumentException("Wallet cannot be null");
        }
        if (company == null) {
            throw new IllegalArgumentException("Company cannot be null");
        }
        if (orderStartDate == null) {
            throw new IllegalArgumentException("Order Start Date cannot be null");
        }
        if (orderExpirationDate != null && orderExpirationDate.before(new Date())) {
            throw new IllegalArgumentException("Order expiration date must be null or in the future");
        }
    }


    public Order createOrder(ActiveOrderProjection activeOrderProjection, OrderType orderType) {
        Wallet wallet = new Wallet();
        wallet.setWalletId(activeOrderProjection.getWalletId());

        Company company = new Company();
        company.setCompanyId(activeOrderProjection.getCompanyId());
        checkOrderConstrains(
                orderType,
                activeOrderProjection.getSharesAmount(),
                activeOrderProjection.getSharePrice(),
                wallet,
                company,
                activeOrderProjection.getOrderStartDate(),
                activeOrderProjection.getOrderExpirationDate()
        );

        Order order = new Order();
        order.setOrderId(activeOrderProjection.getOrderId());
        order.setOrderType(orderType);
        order.setSharesAmount(activeOrderProjection.getSharesAmount());
        order.setSharePrice(activeOrderProjection.getSharePrice());
        order.setWallet(wallet);
        order.setCompany(company);
        order.setOrderStartDate(activeOrderProjection.getOrderStartDate());
        order.setOrderExpirationDate(activeOrderProjection.getOrderExpirationDate());

        return order;
    }

    public Order createOrder(OrderDTO orderDTO, Wallet wallet, Company company) {
        if (!(orderDTO.getOrderType().equals("buy") || orderDTO.getOrderType().equals("sell")))
            throw new IllegalArgumentException("Order type has to be buy/sell");
        OrderType orderType = new OrderType();
        orderType.setOrderType(orderDTO.getOrderType());
        Date orderStartDate = new Date();
        Date orderExpirationDate = null;
        if (orderDTO.getOrderExpirationDate() != null) {
            ZoneId zonedDateTime = ZoneId.of("UTC");
            orderExpirationDate = Date.from(orderDTO.getOrderExpirationDate().atZone(zonedDateTime).toInstant());
        }

        checkOrderConstrains(
                orderType,
                orderDTO.getSharesAmount(),
                orderDTO.getSharePrice(),
                wallet,
                company,
                orderStartDate,
                orderExpirationDate
        );

        Order order = new Order();
        order.setOrderType(orderType);
        order.setSharesAmount(orderDTO.getSharesAmount());
        order.setSharePrice(orderDTO.getSharePrice());
        order.setWallet(wallet);
        order.setCompany(company);
        order.setOrderStartDate(orderStartDate);
        order.setOrderExpirationDate(orderExpirationDate);

        return order;
    }

}
