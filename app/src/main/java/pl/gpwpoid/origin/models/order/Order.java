package pl.gpwpoid.origin.models.order;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.gpwpoid.origin.models.company.Company;
import pl.gpwpoid.origin.models.wallet.Wallet;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Integer orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_type", referencedColumnName = "order_type", nullable = false)
    private OrderType orderType;

    @Positive
    @Column(name = "shares_amount", nullable = false)
    private Integer sharesAmount;

    @Column(name = "order_start_date", nullable = false, columnDefinition = "TIMESTAMP DEFAULT current_timestamp")
    @Temporal(TemporalType.TIMESTAMP)
    private Date orderStartDate;

    @Column(name = "order_expiration_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date orderExpirationDate;


    @Column(name = "share_price", precision = 17, scale = 2)
    private BigDecimal sharePrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id")
    private Wallet wallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OrderCancellation> cancellations;

    @OneToMany(mappedBy = "sellOrder")
    private Set<Transaction> transactionsAsSellOrder;

    @OneToMany(mappedBy = "buyOrder")
    private Set<pl.gpwpoid.origin.models.order.Transaction> transactionsAsBuyOrder;
}
