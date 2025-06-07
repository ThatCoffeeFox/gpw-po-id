package pl.gpwpoid.origin.services.implementations;


import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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
import pl.gpwpoid.origin.repositories.views.TransactionDTO;
import pl.gpwpoid.origin.repositories.views.TransactionWalletListItem;
import pl.gpwpoid.origin.services.CompanyService;
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

    private final CompanyService companyService;

    @Autowired
    public TransactionServiceImpl(TransactionRepository transactionRepository, TransactionFactory transactionFactory, CompanyService companyService, @Lazy ChartUpdateBroadcaster broadcaster){
        this.transactionRepository = transactionRepository;
        this.transactionFactory = transactionFactory;
        this.broadcaster = broadcaster;
        this.companyService = companyService;
    }

    @Override
    @Transactional
    public void addTransaction(Order sellOrder, Order buyOrder, Integer sharesAmount, BigDecimal sharePrice) {
        Transaction newTransaction = transactionFactory.createTransaction(sellOrder, buyOrder, sharesAmount, sharePrice);
        transactionRepository.save(newTransaction);

        Integer companyId = buyOrder.getCompany().getCompanyId();
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

    @Transactional(readOnly = true)
    @Override
    public List<TransactionDTO> getCompanyTransactionDTOListByCompanyId(Integer companyId, Integer limit) {
        if(limit <= 0){
            throw new IllegalArgumentException("Limit has to be positive");
        }

        if(companyService.getCompanyById(companyId).isEmpty()){
            throw new EntityNotFoundException("This company does not exist");
        }

        return transactionRepository.findTransactionDTOListByCompanyId(companyId, limit);
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

    @Transactional(readOnly = true)
    @Override
    public List<TransactionWalletListItem> getTransactionsByCompanyAndUser(int companyId, int userId, Pageable pageable) {
        return transactionRepository.findByCompanyAndUser(companyId, userId, pageable);
    }
}