package pl.gpwpoid.origin.services.implementations;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import pl.gpwpoid.origin.factories.TransactionFactory;
import pl.gpwpoid.origin.models.order.Order;
import pl.gpwpoid.origin.models.order.Transaction;
import pl.gpwpoid.origin.repositories.TransactionRepository;
import pl.gpwpoid.origin.repositories.views.OHLCDataItem;
import pl.gpwpoid.origin.repositories.views.TransactionListItem;
import pl.gpwpoid.origin.repositories.views.TransactionWalletListItem;
import pl.gpwpoid.origin.services.ChartUpdateBroadcaster;
import pl.gpwpoid.origin.services.TransactionService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Service
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final TransactionFactory transactionFactory;
    private final ChartUpdateBroadcaster broadcaster;

    @Autowired
    public TransactionServiceImpl(TransactionRepository transactionRepository, TransactionFactory transactionFactory, ChartUpdateBroadcaster broadcaster) {
        this.transactionRepository = transactionRepository;
        this.transactionFactory = transactionFactory;
        this.broadcaster = broadcaster;
    }

    @Override
    @Transactional
    public void addTransaction(Order sellOrder, Order buyOrder, Integer sharesAmount, BigDecimal sharePrice) {
        Transaction newTransaction = transactionFactory.createTransaction(sellOrder, buyOrder, sharesAmount, sharePrice);
        transactionRepository.save(newTransaction);

        Integer companyId = buyOrder.getCompany().getCompanyId();

        // Użyj TransactionSynchronizationManager, aby wywołać broadcast PO zatwierdzeniu transakcji
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                broadcaster.broadcast(companyId);
            }
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<Transaction> getTransactions() {
        return transactionRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<TransactionListItem> getCompanyTransactionsById(int companyId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return transactionRepository.findTransactionsByIdAsListItems(companyId, pageable);
    }

    @Override
    public Collection<TransactionWalletListItem> getTransactionsByWalletId(int walletId) {
        return transactionRepository.getTransactionsByWalletId(walletId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OHLCDataItem> getOHLCDataByCompanyId(Integer companyId, LocalDateTime from, LocalDateTime to) {
        return transactionRepository.getOHLCDataByCompanyId(companyId, from, to);
    }

    @Transactional(readOnly = true)
    @Override
    public BigDecimal getShareValueByCompanyId(Integer companyId) {
        return transactionRepository.findShareValueByCompanyId(companyId);
    }
}