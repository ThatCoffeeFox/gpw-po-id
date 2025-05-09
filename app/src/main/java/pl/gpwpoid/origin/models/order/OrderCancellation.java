package pl.gpwpoid.origin.models.order;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import pl.gpwpoid.origin.models.keys.OrderCancellationId;

@Entity
@Table(name = "order_cancellations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCancellation {
    @EmbeddedId
    private OrderCancellationId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("orderId")
    @JoinColumn(name = "order_id")
    private pl.gpwpoid.origin.models.order.Order order;
}
