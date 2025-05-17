package pl.gpwpoid.origin.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.gpwpoid.origin.models.keys.TransactionId;
import pl.gpwpoid.origin.models.order.Transaction;
import pl.gpwpoid.origin.repositories.views.TransactionListItem;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, TransactionId> {

    @Query("""
    SELECT new pl.gpwpoid.origin.repositories.views.TransactionListItem(t.date, t.sharesAmount, t.sharePrice)
    FROM Transaction t
    WHERE t.buyOrder.company.id = :companyId
    ORDER BY t.date DESC
    """)
    List<TransactionListItem> findTransactionsByIdAsListItems(@Param("companyId") int companyId, Pageable pageable);

    @Query(value = """
            SELECT shares_value
            FROM shares_value()
            WHERE company_id = :companyId
            """, nativeQuery = true)
    BigDecimal findShareValueByCompanyId(@Param("companyId") Integer companyId);
}
