package pl.gpwpoid.origin.models.wallet;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "type", nullable = false, updatable = false)
    private TransferType type;

    @Column(name = "date", nullable = false, columnDefinition = "TIMESTAMP DEFAULT current_timestamp", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;

    @Positive
    @Column(name = "amount", nullable = false, precision = 17, scale = 2, updatable = false)
    private BigDecimal amount;

    @Column(name = "account_number", nullable = false, updatable = false)
    private String accountNumber;
}
