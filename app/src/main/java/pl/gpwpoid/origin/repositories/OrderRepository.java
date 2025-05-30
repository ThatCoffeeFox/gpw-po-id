package pl.gpwpoid.origin.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.gpwpoid.origin.models.order.Order;
import pl.gpwpoid.origin.repositories.projections.ActiveOrderProjection;
import pl.gpwpoid.origin.repositories.views.ActiveOrderListItem;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query(value = """
    SELECT order_id, shares_left, order_start_date, order_expiration_date,
           share_price, wallet_id, company_id, shares_amount
    FROM active_buy_orders
    WHERE company_id = :companyId
""", nativeQuery = true)
    List<ActiveOrderProjection> findActiveBuyOrdersByCompanyId(@Param("companyId") Integer companyId);

    @Query(value = """
    SELECT order_id, shares_left, order_start_date, order_expiration_date,
           share_price, wallet_id, company_id, shares_amount
    FROM active_sell_orders
    WHERE company_id = :companyId
""", nativeQuery = true)
    List<ActiveOrderProjection> findActiveSellOrdersByCompanyId(@Param("companyId") Integer companyId);

    @Query(value = """
        SELECT order_id, name, order_type, shares_amount, share_price, order_start_date, order_expiration_date
        FROM (
            SELECT abo.order_id, w.name, abo.order_type, abo.shares_amount, abo.share_price, 
                   abo.order_start_date, abo.order_expiration_date
            FROM active_buy_orders abo 
            JOIN wallets w ON abo.wallet_id = w.wallet_id
            WHERE w.account_id = :accountId
            
            UNION ALL
            
            SELECT aso.order_id, w.name, aso.order_type, aso.shares_amount, aso.share_price, 
                   aso.order_start_date, aso.order_expiration_date
            FROM active_sell_orders aso
            JOIN wallets w ON aso.wallet_id = w.wallet_id
            WHERE w.account_id = :accountId
        ) combined_orders
        ORDER BY order_start_date ASC
        """, nativeQuery = true)
    List<ActiveOrderListItem> findActiveOrdersByAccountId(@Param("accountId") Integer accountId);

}
