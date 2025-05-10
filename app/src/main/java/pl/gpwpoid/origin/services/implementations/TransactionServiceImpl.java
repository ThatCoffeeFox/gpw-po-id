package pl.gpwpoid.origin.services.implementations;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.gpwpoid.origin.models.factories.TransactionFactory;
import pl.gpwpoid.origin.models.order.Order;
import pl.gpwpoid.origin.models.order.Transaction;
import pl.gpwpoid.origin.repositories.TransactionRepository;
import pl.gpwpoid.origin.services.TransactionService;

import java.math.BigDecimal;
import java.util.Collection;

@Service
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final TransactionFactory transactionFactory;

    @Autowired
    public TransactionServiceImpl(TransactionRepository transactionRepository, TransactionFactory transactionFactory){
        this.transactionRepository = transactionRepository;
        this.transactionFactory = transactionFactory;
    }

    @Override
    @Transactional
    public void addTransaction(Order sellOrder, Order buyOrder, Integer sharesAmount, BigDecimal sharePrice) {
        Transaction newTransaction = transactionFactory.createTransaction(sellOrder, buyOrder, sharesAmount, sharePrice);
        transactionRepository.save(newTransaction);
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<Transaction> getTransactions() {
        return transactionRepository.findAll();
    }
}