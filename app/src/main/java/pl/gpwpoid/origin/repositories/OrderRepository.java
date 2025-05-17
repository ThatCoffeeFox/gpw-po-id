package pl.gpwpoid.origin.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.gpwpoid.origin.models.order.Order;
import pl.gpwpoid.origin.repositories.projections.ActiveOrderProjection;

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
}
