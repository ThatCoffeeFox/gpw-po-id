package pl.gpwpoid.origin.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.gpwpoid.origin.models.wallet.Wallet;
import pl.gpwpoid.origin.repositories.views.WalletListItem;

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
        SELECT w 
        FROM wallets w 
        JOIN accounts a ON a.account_id = w.account_id
        JOIN accounts_info ai ON a.account_id = ai.account_id
        WHERE ai.email = :email
""", nativeQuery = true)
    List<Wallet> getWalletForCurrentUser(String email);
}
