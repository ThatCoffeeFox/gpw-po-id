package pl.gpwpoid.origin.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.gpwpoid.origin.models.order.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
