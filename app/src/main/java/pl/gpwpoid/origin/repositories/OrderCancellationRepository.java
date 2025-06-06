package pl.gpwpoid.origin.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.gpwpoid.origin.models.keys.OrderCancellationId;
import pl.gpwpoid.origin.models.order.OrderCancellation;

@Repository
public interface OrderCancellationRepository extends JpaRepository<OrderCancellation, OrderCancellationId> {

}
