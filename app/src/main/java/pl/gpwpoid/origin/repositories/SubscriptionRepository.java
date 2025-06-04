package pl.gpwpoid.origin.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.gpwpoid.origin.models.order.Subscription;
import pl.gpwpoid.origin.repositories.views.SubscriptionListItem;

import java.util.List;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Integer> {
    @Query(value = """
            SELECT DISTINCT ON (s.subscription_id)
                s.date,
                w.name,
                c.name,
                s.shares_amount,
                i.ipo_price,
                s.shares_assigned
            FROM subscriptions s JOIN ipo i USING(ipo_id) JOIN companies_info c USING (company_id) JOIN wallets w USING (wallet_id)
            WHERE w.account_id = :accountId
            ORDER BY s.subscription_id,  c.updated_at ASC;
            """, nativeQuery = true)
    List<SubscriptionListItem> findSubscriptionListItemsByAccountId(Integer accountId);
}
