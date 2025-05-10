package pl.gpwpoid.origin.factories;

import org.springframework.stereotype.Component;
import pl.gpwpoid.origin.models.company.Company;
import pl.gpwpoid.origin.models.order.Order;
import pl.gpwpoid.origin.models.order.OrderType;
import pl.gpwpoid.origin.models.wallet.Wallet;
import pl.gpwpoid.origin.ui.views.DTO.OrderDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class OrderFactory {
    public Order createOrder(OrderType orderType,
         int shares_amount,
         BigDecimal sharePrice,
         Wallet wallet,
         Company company,
         Date orderExpirationDate){
        if(orderType == null){
            throw new IllegalArgumentException("Order type cannot be null");
        }
        if(wallet == null){
            throw new IllegalArgumentException("Wallet cannot be null");
        }
        if(company == null){
            throw new IllegalArgumentException("Company cannot be null");
        }
        if (orderExpirationDate != null && orderExpirationDate.before(new Date())) {
            throw new IllegalArgumentException("Order expiration date must be null or in the future");
        }

        Order order = new Order();
        order.setOrderType(orderType);
        order.setSharesAmount(shares_amount);
        order.setSharePrice(sharePrice);
        order.setWallet(wallet);
        order.setCompany(company);
        order.setOrderStartDate(new Date());
        order.setOrderExpirationDate(orderExpirationDate);

        return order;
    }

    public Order createOrder(OrderDTO orderDTO){
        if(orderDTO.getOrderType() == null){
            throw new IllegalArgumentException("Order type cannot be null");
        }
        if(orderDTO.getWallet() == null){
            throw new IllegalArgumentException("Wallet cannot be null");
        }
        if(orderDTO.getCompany() == null){
            throw new IllegalArgumentException("Company cannot be null");
        }
        LocalDateTime localExpirationDate = orderDTO.getDateTime();
        Date expirationDate = null;
        if(localExpirationDate != null){
            ZoneId zoneId = ZoneId.of("UTC");
            expirationDate = Date.from(localExpirationDate.atZone(zoneId).toInstant());
            if(expirationDate.before(new Date())){
                throw new IllegalArgumentException("Order expiration date must be null or in the future");
            }
        }
        Order order = new Order();
        order.setOrderType(orderDTO.getOrderType());
        order.setWallet(orderDTO.getWallet());
        order.setCompany(orderDTO.getCompany());
        order.setSharePrice(orderDTO.getPrice());
        order.setSharesAmount(orderDTO.getAmount());
        order.setOrderStartDate(new Date());
        order.setOrderExpirationDate(expirationDate);

        return order;
    }
}
