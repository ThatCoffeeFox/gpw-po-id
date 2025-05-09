package pl.gpwpoid.origin.models.order;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Set;

@Entity
@Table(name = "order_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderType {
    @Id
    @Column(name = "order_type", length = 32)
    private String orderType; // np. "sell", "buy"

    @OneToMany(mappedBy = "orderType") // Relacja zwrotna do Orders
    private Set<pl.gpwpoid.origin.models.order.Order> orders;
}