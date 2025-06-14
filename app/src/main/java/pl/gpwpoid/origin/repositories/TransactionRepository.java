package pl.gpwpoid.origin.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.gpwpoid.origin.models.keys.TransactionId;
import pl.gpwpoid.origin.models.order.Transaction;
import pl.gpwpoid.origin.repositories.DTO.TransactionDTO;
import pl.gpwpoid.origin.repositories.views.OHLCDataItem;
import pl.gpwpoid.origin.repositories.views.TransactionWalletListItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, TransactionId> {

    @Query(value = """
            SELECT :companyId, t.date, t.shares_amount, t.share_price
            FROM transactions t JOIN orders o ON t.buy_order_id = o.order_id
            WHERE o.company_id = :companyId
            ORDER BY t.date DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<TransactionDTO> findTransactionDTOListByCompanyId(@Param("companyId") int companyId, Integer limit);

    @Query(value = """
            SELECT shares_value( :companyId )
            """, nativeQuery = true)
    BigDecimal findShareValueByCompanyId(@Param("companyId") Integer companyId);

    @Query(value = """
                SELECT shares_value_last_day(:companyId)
            """, nativeQuery = true)
    BigDecimal findShareValueLastDayByCompany(@Param("companyId") Integer companyId);

    @Query(value = """
                WITH CompanyTransactions AS (
                    SELECT
                        t.date AS transaction_time,
                        t.share_price,
                        o.company_id,
                        ROW_NUMBER() OVER (PARTITION BY o.company_id, DATE_TRUNC('second', t.date) ORDER BY t.date ASC) as rn_asc,
                        ROW_NUMBER() OVER (PARTITION BY o.company_id, DATE_TRUNC('second', t.date) ORDER BY t.date DESC) as rn_desc
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
                        DATE_TRUNC('second', ct.transaction_time) AS ohlc_date,
                        ct.company_id,
                        MAX(ct.share_price) AS high_price,
                        MIN(ct.share_price) AS low_price
                    FROM
                        CompanyTransactions ct
                    GROUP BY
                        DATE_TRUNC('second', ct.transaction_time),
                        ct.company_id
                ),
                OpenPrices AS (
                    SELECT
                        DATE_TRUNC('second', ct.transaction_time) AS ohlc_date,
                        ct.company_id,
                        ct.share_price AS open_price
                    FROM
                        CompanyTransactions ct
                    WHERE
                        ct.rn_asc = 1
                ),
                ClosePrices AS (
                    SELECT
                        DATE_TRUNC('second', ct.transaction_time) AS ohlc_date,
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


    @Query(value = """
                    SELECT
                        o.order_type AS orderType,
                        t.date AS date,
                        t.shares_amount*t.share_price AS amount,
                        t.shares_amount AS shares_amount,
                        ci.code AS companyCode,
                        ci.company_id AS companyId,
                        :walletId as walletId
                    FROM transactions t
                    JOIN orders o ON t.buy_order_id = o.order_id OR t.sell_order_id = o.order_id
                    JOIN companies_info ci ON ci.company_id = o.company_id
                    WHERE o.wallet_id = :walletId AND
                        ci.updated_at = (SELECT cii.updated_at FROM companies_info cii WHERE cii.company_id = ci.company_id
                                        ORDER BY t.date DESC LIMIT 1)
            """, nativeQuery = true)
    List<TransactionWalletListItem> getTransactionsByWalletId(int walletId);

    boolean existsByBuyOrder_Company_CompanyIdAndDateAfter(Integer companyId, LocalDateTime since);

    @Query(value = """
                SELECT
                    o.order_type AS orderType,
                    t.date AS date,
                    t.shares_amount*t.share_price AS amount,
                    t.shares_amount AS shares_amount,
                    ci.code as companyCode,
                    ci.company_id AS companyId,
                    w.wallet_id as walletId
                FROM transactions t
                JOIN orders o ON (t.buy_order_id = o.order_id OR t.sell_order_id = o.order_id)
                JOIN wallets w ON (w.wallet_id = o.wallet_id)
                JOIN companies_info ci ON (ci.company_id = o.company_id)
                WHERE w.account_id = :userId
                AND o.company_id = :companyId
                AND ci.updated_at = (SELECT cii.updated_at FROM companies_info cii WHERE cii.company_id = ci.company_id ORDER BY t.date DESC LIMIT 1)
                ORDER BY t.date DESC
            """, nativeQuery = true)
    List<TransactionWalletListItem> findByCompanyAndUser(@Param("companyId") int companyId,
                                                         @Param("userId") int userId,
                                                         Pageable pageable);

    @Query(value = """
            SELECT t.share_price
            FROM transactions t JOIN orders o ON t.buy_order_id = o.order_id
            WHERE o.company_id = :companyId AND t.date < :beforeDate
            ORDER BY t.date DESC
            LIMIT 1
            """, nativeQuery = true)
    Optional<BigDecimal> findLastSharePriceBeforeDate(@Param("companyId") int companyId, @Param("beforeDate") LocalDateTime beforeDate);

    @Query("SELECT new pl.gpwpoid.origin.repositories.DTO.TransactionDTO(t.sellOrder.company.companyId, t.date, t.sharesAmount, t.sharePrice) FROM Transaction t WHERE t.buyOrder.wallet.account.accountId = :accountId OR t.sellOrder.wallet.account.accountId = :accountId ORDER BY t.date DESC")
    List<TransactionDTO> findLatestTransactionsByAccountId(Integer accountId, Pageable pageable);
}
