package pl.gpwpoid.origin.models.company;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.gpwpoid.origin.models.order.Subscription;
import pl.gpwpoid.origin.models.wallet.Wallet;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "ipo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IPO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ipo_id")
    private Integer ipoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private pl.gpwpoid.origin.models.company.Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_wallet_id")
    private Wallet paymentWallet;

    @Positive
    @Column(name = "shares_amount", nullable = false)
    private Integer sharesAmount;

    @Positive
    @Column(name = "ipo_price", nullable = false, precision = 17, scale = 2)
    private BigDecimal ipoPrice;

    @Column(name = "subscription_start", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date subscriptionStart;

    @Column(name = "subscription_end", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date subscriptionEnd;

    @Column(name = "processed", nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean processed;


    @OneToMany(mappedBy = "ipo")
    private Set<Subscription> subscriptions;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IPO that = (IPO) o;
        return Objects.equals(ipoId, that.ipoId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ipoId);
    }
}