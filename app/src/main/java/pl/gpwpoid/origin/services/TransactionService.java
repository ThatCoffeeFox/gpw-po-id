package pl.gpwpoid.origin.services;

import org.springframework.transaction.annotation.Transactional;
import pl.gpwpoid.origin.models.order.Order;
import pl.gpwpoid.origin.models.order.Transaction;
import pl.gpwpoid.origin.repositories.views.OHLCDataItem;
import pl.gpwpoid.origin.repositories.views.TransactionDTO;
import pl.gpwpoid.origin.repositories.views.TransactionWalletListItem;

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

    @Transactional(readOnly = true)
    List<TransactionDTO> getCompanyTransactionDTOListByCompanyId(Integer companyId, Integer limit);

    Collection<TransactionWalletListItem> getTransactionsByWalletId(int walletId);
    List<OHLCDataItem> getOHLCDataByCompanyId(Integer companyId, LocalDateTime from, LocalDateTime to);

    @Transactional(readOnly = true)
    public BigDecimal getShareValueByCompanyId(Integer companyId);
}
