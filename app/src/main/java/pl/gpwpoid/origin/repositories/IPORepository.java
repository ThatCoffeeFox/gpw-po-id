package pl.gpwpoid.origin.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.gpwpoid.origin.models.company.IPO;
import pl.gpwpoid.origin.repositories.views.IPOListItem;

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
}