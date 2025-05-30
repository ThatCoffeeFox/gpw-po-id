package pl.gpwpoid.origin.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.gpwpoid.origin.models.wallet.Wallet;
import pl.gpwpoid.origin.repositories.views.WalletCompanyListItem;
import pl.gpwpoid.origin.repositories.views.WalletListItem;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface WalletRepository extends JpaRepository<Wallet,Long> {
    @Query(value = """
        SELECT 
            w.wallet_id AS walletId,
            w.name AS name,
            funds_in_wallet(w.wallet_id) AS funds
        FROM wallets w
        JOIN accounts a ON a.account_id = w.account_id
        WHERE a.account_id = :accountId
""", nativeQuery = true)
    List<WalletListItem> getWalletListViewForCurrentUser(Integer accountId);

    @Query(value = """
        SELECT 
            w.*
        FROM wallets w 
        JOIN accounts a ON a.account_id = w.account_id
        WHERE a.account_id = :accountId
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
            SELECT unblocked_founds_before_market_buy_order( :orderId )
            """, nativeQuery = true)
    BigDecimal getWalletUnblockedFoundsBeforeMarketBuyOrder(Integer orderId);

    @Query(value = """
        SELECT 
            ci.name AS companyName,
            ci.code AS companyCode,
            shares_value(ci.company_id) AS sharesValue,
            shares_in_wallet(:walletId, ci.company_id) AS sharesAmount
        FROM companies_info ci 
        WHERE ci.updated_at = (SELECT cii.updated_at FROM companies_info cii WHERE cii.company_id = ci.company_id
                            ORDER BY cii.updated_at DESC LIMIT 1)
        AND shares_in_wallet(:walletId, ci.company_id) != 0
""", nativeQuery = true)
    List<WalletCompanyListItem> getWalletCompanyListForCurrentWallet(Integer walletId);
}
