package pl.gpwpoid.origin.ui.views.DTO;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.gpwpoid.origin.models.wallet.ExternalTransfer;

import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
public class TransferDTO {
    @Size(min = 26, max = 26, message = "nieprawidłowy numer konta")
    private String accountNumber;

    @Positive(message = "kwota musi być dodatnia")
    @Digits(integer = 17, fraction = 2, message = "kwota może mieć dwa miejsca po przecinku")
    private BigDecimal funds;

    @NotNull
    private Integer walletId;

    @NotNull
    private ExternalTransfer.TransferType transferType;

    @NotNull
    private Date transferDate;
}
