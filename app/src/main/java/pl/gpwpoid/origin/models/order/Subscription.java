package pl.gpwpoid.origin.models.order;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import pl.gpwpoid.origin.models.wallet.Wallet;
import pl.gpwpoid.origin.models.company.IPO;

import java.util.Date;

@Entity
@Table(name = "subscriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subscription_id")
    private Integer subscriptionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ipo_id")
    private IPO ipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id")
    private Wallet wallet;

    @Column(name = "date", nullable = false, columnDefinition = "TIMESTAMP DEFAULT current_timestamp")
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;

    @Positive
    @Column(name = "shares_amount", nullable = false)
    private Integer sharesAmount;

    @PositiveOrZero
    @Column(name = "shares_assigned")
    private Integer sharesAssigned;
}
