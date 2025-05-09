package pl.gpwpoid.origin.services;

import pl.gpwpoid.origin.models.order.Order;
import pl.gpwpoid.origin.models.order.Transaction;

import java.math.BigDecimal;
import java.util.Collection;

public interface TransactionService {
    void addTransaction(Order sellOrder,
                        Order buyOrder,
                        Integer sharesAmount,
                        BigDecimal sharePrice);

    Collection<Transaction> getTransactions();
}
