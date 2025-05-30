package pl.gpwpoid.origin.services;

import org.springframework.transaction.annotation.Transactional;
import pl.gpwpoid.origin.models.order.Order;
import pl.gpwpoid.origin.models.order.Transaction;
import pl.gpwpoid.origin.repositories.views.TransactionListItem;

import java.math.BigDecimal;
import java.util.Collection;

public interface TransactionService {
    void addTransaction(Order sellOrder,
                        Order buyOrder,
                        Integer sharesAmount,
                        BigDecimal sharePrice);

    Collection<Transaction> getTransactions();
    Collection<TransactionListItem> getCompanyTransactionsById(int companyId, int limit);

    @Transactional(readOnly = true)
    public BigDecimal getShareValueByCompanyId(Integer companyId);
}
