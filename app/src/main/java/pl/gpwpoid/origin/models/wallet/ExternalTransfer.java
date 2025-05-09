package pl.gpwpoid.origin.models.wallet;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import pl.gpwpoid.origin.models.wallet.Wallet;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "external_transfers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExternalTransfer {

    public enum TransferType {
        deposit, withdrawal
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transfer_id")
    private Integer transferId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false, updatable = false)
    private Wallet wallet;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, columnDefinition = "transfer_type", updatable = false)
    private TransferType type;

    @Column(name = "date", nullable = false, columnDefinition = "TIMESTAMP DEFAULT current_timestamp", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;

    @Positive
    @Column(name = "amount", nullable = false, precision = 17, scale = 2, updatable = false)
    private BigDecimal amount;
}
