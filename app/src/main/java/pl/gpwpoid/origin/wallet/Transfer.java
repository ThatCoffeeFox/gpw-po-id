package pl.gpwpoid.origin.wallet;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "external_transfers")
public class Transfer {
    public enum transferType {
        deposit,
        withdrawal
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transfer_id")
    private Long id;

    @ManyToOne
    @MapsId("id")
    @JoinColumn(name = "walled_id", nullable = false)
    private Wallet wallet;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private transferType type;

    @Column(name = "date", nullable = false)
    private OffsetDateTime date;

    @Column(name = "amount")
    private Double amount;
}
