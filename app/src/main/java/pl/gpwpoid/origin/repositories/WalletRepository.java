package pl.gpwpoid.origin.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.gpwpoid.origin.models.wallet.Wallet;
import pl.gpwpoid.origin.repositories.DTO.WalletCompanyDTO;
import pl.gpwpoid.origin.repositories.views.TransferListItem;
import pl.gpwpoid.origin.repositories.views.WalletCompanyListItem;
import pl.gpwpoid.origin.repositories.views.WalletListItem;
import pl.gpwpoid.origin.repositories.views.WalletListViewItem;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    @Query(value = """
                    SELECT 
                        w.wallet_id AS walletId,
                        w.name AS name,
                        funds_in_wallet(w.wallet_id) AS funds
                    FROM wallets w
                    JOIN accounts a ON a.account_id = w.account_id
                    WHERE a.account_id = :accountId AND w.active = true
            """, nativeQuery = true)
    List<WalletListItem> getWalletListViewByAccountId(Integer accountId);

    @Query(value = """
                    SELECT 
                        w.wallet_id AS walletId,
                        w.name AS name,
                        funds_in_wallet(w.wallet_id) AS funds,
                        SUM(shares_in_wallet(w.wallet_id, c.company_id)) AS shares,
                        SUM(shares_in_wallet(w.wallet_id, c.company_id)*shares_value(c.company_id)) AS shares_value
                    FROM wallets w
                    JOIN accounts a ON a.account_id = w.account_id
                    CROSS JOIN companies c
                    WHERE a.account_id = :accountId AND w.active = true
                    GROUP BY w.wallet_id, w.name, w.account_id
            """, nativeQuery = true)
    List<WalletListViewItem> getExtendedWalletListViewByAccountId(Integer accountId);

    @Query(value = """
                    SELECT 
                        w.*
                    FROM wallets w 
                    JOIN accounts a ON a.account_id = w.account_id
                    WHERE a.account_id = :accountId AND w.active = true
            """, nativeQuery = true)
    List<Wallet> getWalletForCurrentUser(Integer accountId);

    @Query(value = """ 
                    SELECT unblocked_funds_in_wallet(:walletId) 
            """, nativeQuery = true)
    BigDecimal getWalletUnblockedFundsById(Integer walletId);

    @Query(value = """
                    SELECT shares_in_wallet(:walletId, :companyId) - blocked_shares_in_wallet(:walletId, :companyId) 
            """, nativeQuery = true)
    Integer getWalletUnblockedSharesAmount(Integer walletId, Integer companyId);

    @Query(value = """
            SELECT unblocked_funds_before_market_buy_order( :orderId )
            """, nativeQuery = true)
    BigDecimal getWalletUnblockedFundsBeforeMarketBuyOrder(Integer orderId);

    @Query(value = """
                    SELECT 
                        ci.name AS companyName,
                        ci.code AS companyCode,
                        shares_value(ci.company_id) AS currentSharePrice,
                        shares_value_last_day(ci.company_id) AS previousSharePrice,
                        shares_in_wallet(:walletId, ci.company_id) AS sharesAmount,
                        ci.company_id AS companyId
                    FROM companies_info ci 
                    WHERE ci.updated_at = (SELECT cii.updated_at FROM companies_info cii WHERE cii.company_id = ci.company_id
                                        ORDER BY cii.updated_at DESC LIMIT 1)
                    AND shares_in_wallet(:walletId, ci.company_id) != 0
            """, nativeQuery = true)
    List<WalletCompanyListItem> getWalletCompanyListForCurrentWallet(Integer walletId);

    @Query(value = """
                    SELECT funds_in_wallet(:walletId)
            """, nativeQuery = true)
    BigDecimal getFundsByWalletId(Integer walletId);

    @Query(value = """
                    SELECT w.name
                    FROM wallets w WHERE w.wallet_id = :walletId
            """, nativeQuery = true)
    String getWalletNameById(Integer walletId);

    @Query(value = """
                    SELECT 
                        t.date AS date,
                        CASE WHEN t.type = 'withdrawal' THEN -t.amount ELSE t.amount END AS amount,
                        t.account_number AS accountNumber
                        FROM external_transfers t
                        WHERE t.wallet_id = :walletId
            """, nativeQuery = true)
    List<TransferListItem> getTransferListForCurrentWallet(Integer walletId);

    @Query(value = """
        SELECT 
            w.wallet_id AS walletId,
            w.name AS name,
            funds_in_wallet(w.wallet_id) AS funds
        FROM wallets w
        WHERE w.wallet_id = :walletId AND w.active = true
""", nativeQuery = true)
    WalletListItem getWalletListItemById(Integer walletListItemId);

    @Query(value = """
           SELECT
            :walletId,
            funds_in_wallet(:walletId),
            unblocked_funds_in_wallet(:walletId),
            :companyId,
            shares_in_wallet(:walletId, :companyId),
            shares_in_wallet(:walletId, :companyId) - blocked_shares_in_wallet(:walletId, :companyId),
            shares_value(:companyId);
           """, nativeQuery = true)
    WalletCompanyDTO findWalletCompanyDTOByWalletIdCompanyId(Integer walletId, Integer companyId);
}
