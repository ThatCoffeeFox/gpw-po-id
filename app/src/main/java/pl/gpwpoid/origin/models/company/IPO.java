package pl.gpwpoid.origin.models.company;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import pl.gpwpoid.origin.models.wallet.Wallet;
import pl.gpwpoid.origin.models.order.Subscription;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "ipo") // IPO jest słowem kluczowym w niektórych dialektach SQL, ale nazwa tabeli jest małą literą
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IPO { // Nazwa klasy IPO, bo IPOEntity wyglądałoby dziwnie
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
    // CHECK (subscription_start < subscription_end) - walidacja na poziomie DB lub serwisu

    @OneToMany(mappedBy = "ipo")
    private Set<Subscription> subscriptions;
}