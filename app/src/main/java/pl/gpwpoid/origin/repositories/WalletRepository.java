package pl.gpwpoid.origin.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.gpwpoid.origin.models.wallet.Wallet;
import pl.gpwpoid.origin.repositories.views.WalletListItem;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface WalletRepository extends JpaRepository<Wallet,Long> {
    @Query(value = """
        SELECT 
            w.wallet_id AS walletId,
            w.name AS name,
            f.funds AS funds
        FROM wallets w JOIN funds_in_wallets() f ON w.wallet_id = f.wallet_id
        JOIN accounts a ON a.account_id = w.account_id JOIN accounts_info ai ON a.account_id = ai.account_id
        WHERE ai.email = :email;
""", nativeQuery = true)
    List<WalletListItem> getWalletListViewForCurrentUser(String email);

    @Query(value = """
        SELECT 
            w.*
        FROM wallets w 
        JOIN accounts a ON a.account_id = w.account_id
        JOIN accounts_info ai ON a.account_id = ai.account_id
        WHERE ai.email = :email
""", nativeQuery = true)
    List<Wallet> getWalletForCurrentUser(String email);

    @Query(value = """ 
        SELECT unblocked_funds 
        FROM ublocked_funds_in_wallets()
        WHERE wallet_id = :walletId
""", nativeQuery = true)
    BigDecimal getWalletUnblockedFundsById(Integer walletId);

    @Query(value = """
        SELECT s.shares_amount - b.blocked_shares 
        FROM blocked_shares_in_wallets() b JOIN shares_in_wallets() s ON s.wallet_id = b.wallet_id AND s.company_id = b.company_id
        WHERE wallet_id = :walletId AND company_id = :companyId
""", nativeQuery = true)
    Integer getWalletUnblockedSharesAmount(Integer walletId, Integer companyId);
}
