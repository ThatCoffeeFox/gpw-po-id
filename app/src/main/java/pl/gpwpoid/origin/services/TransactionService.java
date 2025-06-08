package pl.gpwpoid.origin.services;

import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import pl.gpwpoid.origin.models.order.Order;
import pl.gpwpoid.origin.models.order.Transaction;
import pl.gpwpoid.origin.repositories.DTO.TransactionDTO;
import pl.gpwpoid.origin.repositories.views.OHLCDataItem;
import pl.gpwpoid.origin.repositories.views.TransactionWalletListItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

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
    BigDecimal getShareValueByCompanyId(Integer companyId);

    @Transactional(readOnly = true)
    List<TransactionWalletListItem> getTransactionsByCompanyAndUser(int companyId, int userId, Pageable pageable);

    Optional<BigDecimal> findLastSharePriceBeforeDate(Integer companyId, LocalDateTime beforeDate);

    @Transactional(readOnly = true)
    List<TransactionDTO> getLatestTransactionsByAccountId(Integer accountId, Pageable pageable);
}
