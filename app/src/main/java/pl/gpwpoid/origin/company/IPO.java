package pl.gpwpoid.origin.company;

import jakarta.persistence.*;
import pl.gpwpoid.origin.wallet.Wallet;

import java.time.OffsetDateTime;

@Entity
@Table(name = "ipo")
public class IPO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ipo_id")
    private Long id;

    @ManyToOne
    @MapsId("id")
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne
    @MapsId("id")
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet paymentWallet;

    @Column(name = "shares_amount")
    private Integer sharesAmount;

    @Column(name = "ipo_price")
    private Double price;

    @Column(name = "subscription_start")
    private OffsetDateTime subscriptionStart;

    @Column(name = "subscription_end")
    private OffsetDateTime subscriptionEnd;

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Wallet getPaymentWallet() {
        return paymentWallet;
    }

    public void setPaymentWallet(Wallet paymentWallet) {
        this.paymentWallet = paymentWallet;
    }

    public Integer getSharesAmount() {
        return sharesAmount;
    }

    public void setSharesAmount(Integer sharesAmount) {
        this.sharesAmount = sharesAmount;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public OffsetDateTime getSubscriptionStart() {
        return subscriptionStart;
    }

    public void setSubscriptionStart(OffsetDateTime subscriptionStart) {
        this.subscriptionStart = subscriptionStart;
    }

    public OffsetDateTime getSubscriptionEnd() {
        return subscriptionEnd;
    }

    public void setSubscriptionEnd(OffsetDateTime subscriptionEnd) {
        this.subscriptionEnd = subscriptionEnd;
    }
}
