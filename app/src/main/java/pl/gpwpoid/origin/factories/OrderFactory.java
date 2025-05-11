package pl.gpwpoid.origin.factories;

import org.springframework.stereotype.Component;
import pl.gpwpoid.origin.models.company.Company;
import pl.gpwpoid.origin.models.order.Order;
import pl.gpwpoid.origin.models.order.OrderType;
import pl.gpwpoid.origin.models.wallet.Wallet;

import java.math.BigDecimal;
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
        if(shares_amount <= 0){
            throw new IllegalArgumentException("Shars ammount has to be grater than zero");
        }
        if(sharePrice != null && sharePrice.compareTo(BigDecimal.valueOf(0)) <= 0){
            throw  new IllegalArgumentException("Share price has to be null or greater than zero");
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
}
