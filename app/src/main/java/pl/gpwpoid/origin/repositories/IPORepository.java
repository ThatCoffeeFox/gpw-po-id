package pl.gpwpoid.origin.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.gpwpoid.origin.models.company.IPO;
import pl.gpwpoid.origin.repositories.views.AdminIPOListItem;
import pl.gpwpoid.origin.repositories.views.IPOListItem;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface IPORepository extends JpaRepository<IPO, Long> {
    @Query(value = """
            SELECT DISTINCT ON (i.ipo_id)
                i.ipo_id,
                i.company_id,
                c.name,
                i.shares_amount,
                i.ipo_price,
                i.subscription_start,
                i.subscription_end
            FROM ipo i JOIN companies_info c USING (company_id)
            WHERE current_timestamp BETWEEN i.subscription_start AND i.subscription_end
            ORDER BY i.ipo_id,  c.updated_at ASC;
            """, nativeQuery = true)
    List<IPOListItem> findActiveIPOListItems();

    @Query(value = """
        SELECT 
            ai.first_name || ' ' || ai.last_name AS walletOwner,
            i.shares_amount AS sharesAmount,
            i.ipo_price AS ipoPrice,
            i.subscription_start AS subscriptionStart,
            i.subscription_end AS subscriptionEnd
        FROM ipo i JOIN wallets w ON w.wallet_id = i.payment_wallet_id
        JOIN accounts_info ai ON ai.account_id = w.account_id
        WHERE i.company_id = :companyId
        AND ai.updated_at = (
            SELECT aii.updated_at
            FROM accounts_info aii 
            WHERE aii.account_id = ai.account_id
            ORDER BY aii.updated_at ASC
            LIMIT 1
        )
""", nativeQuery = true)
    List<AdminIPOListItem> getAdminIPOListItemsByCompanyId(Integer companyId);
}