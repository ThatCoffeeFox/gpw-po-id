package pl.gpwpoid.origin.models.order;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.gpwpoid.origin.models.keys.TransactionId;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    @EmbeddedId
    private TransactionId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("sellOrderId")
    @JoinColumn(name = "sell_order_id")
    private Order sellOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("buyOrderId")
    @JoinColumn(name = "buy_order_id")
    private Order buyOrder;

    @Column(name = "date", nullable = false, columnDefinition = "TIMESTAMP DEFAULT current_timestamp")
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;

    @Positive
    @Column(name = "shares_amount", nullable = false)
    private Integer sharesAmount;

    @Column(name = "share_price", nullable = false, precision = 17, scale = 2)
    private BigDecimal sharePrice;

}
