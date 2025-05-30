package pl.gpwpoid.origin.services;

import pl.gpwpoid.origin.models.order.Order;
import pl.gpwpoid.origin.models.order.Transaction;
import pl.gpwpoid.origin.repositories.views.OHLCDataItem;
import pl.gpwpoid.origin.repositories.views.TransactionListItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface TransactionService {
    void addTransaction(Order sellOrder,
                        Order buyOrder,
                        Integer sharesAmount,
                        BigDecimal sharePrice);

    Collection<Transaction> getTransactions();
    Collection<TransactionListItem> getCompanyTransactionsById(int companyId, int limit);
    List<OHLCDataItem> getOHLCDataByCompanyId(Integer companyId, LocalDateTime from, LocalDateTime to);
}
