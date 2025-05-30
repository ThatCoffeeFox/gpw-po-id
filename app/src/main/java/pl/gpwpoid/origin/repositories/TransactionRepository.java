package pl.gpwpoid.origin.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.gpwpoid.origin.models.keys.TransactionId;
import pl.gpwpoid.origin.models.order.Transaction;
import pl.gpwpoid.origin.repositories.views.OHLCDataItem;
import pl.gpwpoid.origin.repositories.views.TransactionListItem;

import java.time.LocalDateTime;
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
            SELECT shares_value( :companyId )
            """, nativeQuery = true)
    BigDecimal findShareValueByCompanyId(@Param("companyId") Integer companyId);

    @Query(value = """
        WITH CompanyTransactions AS (
            SELECT
                t.date AS transaction_time,
                t.share_price,
                o.company_id,
                ROW_NUMBER() OVER (PARTITION BY o.company_id, DATE_TRUNC('day', t.date) ORDER BY t.date ASC) as rn_asc,
                ROW_NUMBER() OVER (PARTITION BY o.company_id, DATE_TRUNC('day', t.date) ORDER BY t.date DESC) as rn_desc
            FROM
                transactions t
            JOIN
                orders o ON t.sell_order_id = o.order_id
            WHERE
                o.company_id = :companyId
                AND t.date >= :startDate 
                AND t.date < :endDate
        ),
        DailyAggregates AS (
            SELECT
                DATE_TRUNC('day', ct.transaction_time) AS ohlc_date,
                ct.company_id,
                MAX(ct.share_price) AS high_price,
                MIN(ct.share_price) AS low_price
            FROM
                CompanyTransactions ct
            GROUP BY
                DATE_TRUNC('day', ct.transaction_time),
                ct.company_id
        ),
        OpenPrices AS (
            SELECT
                DATE_TRUNC('day', ct.transaction_time) AS ohlc_date,
                ct.company_id,
                ct.share_price AS open_price
            FROM
                CompanyTransactions ct
            WHERE
                ct.rn_asc = 1
        ),
        ClosePrices AS (
            SELECT
                DATE_TRUNC('day', ct.transaction_time) AS ohlc_date,
                ct.company_id,
                ct.share_price AS close_price
            FROM
                CompanyTransactions ct
            WHERE
                ct.rn_desc = 1
        )
        SELECT
            da.ohlc_date AS "timestamp",
            op.open_price AS "open",
            da.high_price AS "high",
            da.low_price AS "low",
            cp.close_price AS "close"
        FROM
            DailyAggregates da
        JOIN
            OpenPrices op ON da.ohlc_date = op.ohlc_date AND da.company_id = op.company_id
        JOIN
            ClosePrices cp ON da.ohlc_date = cp.ohlc_date AND da.company_id = cp.company_id
        ORDER BY
            da.ohlc_date ASC;
    """, nativeQuery = true)
    List<OHLCDataItem> getOHLCDataByCompanyId(@Param("companyId") int companyId,
                                              @Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);

}
