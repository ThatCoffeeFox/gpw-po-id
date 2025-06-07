package pl.gpwpoid.origin.models.order;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Entity
@Table(name = "order_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderType {
    @Id
    @Column(name = "order_type", length = 32)
    private String orderType;

    @OneToMany(mappedBy = "orderType")
    private Set<pl.gpwpoid.origin.models.order.Order> orders;
}