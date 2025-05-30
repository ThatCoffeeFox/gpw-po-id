package pl.gpwpoid.origin.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.gpwpoid.origin.models.keys.TransactionId;
import pl.gpwpoid.origin.models.order.Transaction;
import pl.gpwpoid.origin.repositories.views.TransactionListItem;
import pl.gpwpoid.origin.repositories.views.TransactionWalletListItem;

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
        SELECT
            o.order_type AS orderType,
            t.date AS date,
            t.shares_amount*t.share_price AS amount,
            t.shares_amount AS shares_amount,
            ci.code AS companyCode,
            ci.company_id AS companyId
        FROM transactions t
        JOIN orders o ON t.buy_order_id = o.order_id OR t.sell_order_id = o.order_id
        JOIN companies_info ci ON ci.company_id = o.company_id
        WHERE o.wallet_id = :walletId AND
            ci.updated_at = (SELECT cii.updated_at FROM companies_info cii WHERE cii.company_id = ci.company_id
                            ORDER BY t.date DESC LIMIT 1)
""", nativeQuery = true)
    List<TransactionWalletListItem> getTransactionsByWalletId(int walletId);
}
