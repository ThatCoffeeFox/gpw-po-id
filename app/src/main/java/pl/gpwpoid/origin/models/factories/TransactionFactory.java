package pl.gpwpoid.origin.models.factories;

import org.springframework.stereotype.Component;
import pl.gpwpoid.origin.models.keys.TransactionId;
import pl.gpwpoid.origin.models.order.Order;
import pl.gpwpoid.origin.models.order.Transaction;

import java.math.BigDecimal;
import java.util.Date;

@Component
public class TransactionFactory {

    public Transaction createTransaction(
            Order sellOrder,
            Order buyOrder,
            Integer sharesAmount,
            BigDecimal sharePrice
    ) {
       if(sellOrder == null)
           throw new IllegalArgumentException("sellOrder cannot be null");
       if(buyOrder == null)
           throw new IllegalArgumentException("buyOrder cannot be null");
       if(sharesAmount == null)
           throw new IllegalArgumentException("sharesAmount cannot be null");
       if(sharePrice == null || sharesAmount < 0)
           throw new IllegalArgumentException("sharePrice cannot be null or negative");

       Transaction transaction = new Transaction();
       transaction.setSellOrder(sellOrder);
       transaction.setBuyOrder(buyOrder);
       transaction.setDate(new Date());
       transaction.setSharePrice(sharePrice);
       transaction.setSharesAmount(sharesAmount);
       transaction.setId(new TransactionId(sellOrder.getOrderId(), buyOrder.getOrderId()));

       return transaction;
    }
}
