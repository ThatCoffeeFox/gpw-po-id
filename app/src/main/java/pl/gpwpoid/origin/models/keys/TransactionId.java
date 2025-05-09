package pl.gpwpoid.origin.models.keys;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionId implements Serializable {
    @Column(name = "sell_order_id")
    private Integer sellOrderId;

    @Column(name = "buy_order_id")
    private Integer buyOrderId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionId that = (TransactionId) o;
        return Objects.equals(sellOrderId, that.sellOrderId) && Objects.equals(buyOrderId, that.buyOrderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sellOrderId, buyOrderId);
    }
}
