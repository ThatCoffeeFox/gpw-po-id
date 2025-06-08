package pl.gpwpoid.origin.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.gpwpoid.origin.models.order.Order;
import pl.gpwpoid.origin.repositories.DTO.ActiveOrderDTO;
import pl.gpwpoid.origin.repositories.projections.ActiveOrderProjection;
import pl.gpwpoid.origin.repositories.views.ActiveOrderListItem;

import java.util.List;

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
                WHERE w.account_id = :accountId AND abo.company_id = :companyId 
            
                UNION ALL
            
                SELECT aso.order_id, w.name, aso.order_type, aso.shares_amount, aso.share_price, 
                       aso.order_start_date, aso.order_expiration_date
                FROM active_sell_orders aso
                JOIN wallets w ON aso.wallet_id = w.wallet_id
                WHERE w.account_id = :accountId AND aso.company_id = :companyId 
            ) combined_orders
            ORDER BY order_start_date DESC
            """, nativeQuery = true)
    List<ActiveOrderListItem> findActiveOrdersByAccountIdCompanyId(@Param("accountId") Integer accountId, Integer companyId, Pageable pageable);

    @Query(value = """
            SELECT order_id, order_type, shares_amount, share_price, order_start_date, order_expiration_date
            FROM (
                SELECT abo.order_id, abo.order_expiration_date, abo.order_type, abo.shares_amount, abo.share_price, abo.order_start_date
                FROM active_buy_orders abo
                WHERE abo.wallet_id = :walletId AND abo.company_id = :companyId
            
                UNION ALL
            
                SELECT aso.order_id, aso.order_expiration_date, aso.order_type, aso.shares_amount, aso.share_price, aso.order_start_date
                FROM active_sell_orders aso
                WHERE aso.wallet_id = :walletId AND aso.company_id = :companyId
            ) combined_orders
            ORDER BY order_start_date DESC
            """, nativeQuery = true)
    List<ActiveOrderDTO> findActiveOrderDTOListByWalletIdCompanyId(Integer walletId, Integer companyId);

    @Query(value = """
            SELECT is_canceled_order(:orderId)
            """, nativeQuery = true)
    Boolean isCanceledOrder(Integer orderId);

    @Query(value = """
                SELECT shares_left_in_order(:orderId)
            """, nativeQuery = true)
    Integer getSharesLeft(Integer orderId);

    @Query(value = """
                SELECT o.order_id, w.name, o.order_type, o.shares_amount, o.share_price, o.order_start_date, o.order_expiration_date
                FROM orders o JOIN wallets w ON o.wallet_id = w.wallet_id
                WHERE w.account_id = :accountId
                ORDER BY order_start_date DESC
            """, nativeQuery = true)
    List<ActiveOrderListItem> findOrdersByAccountId(@Param("accountId") Integer accountId, Pageable pageable);
}
