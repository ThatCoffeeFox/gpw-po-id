package pl.gpwpoid.origin.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.gpwpoid.origin.models.order.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

}
