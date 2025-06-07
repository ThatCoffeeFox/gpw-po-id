package pl.gpwpoid.origin.ui.views.DTO;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@NoArgsConstructor
public class IPODTO {
    @NotNull(message = "Właściciel portfela jest wymagany")
    @Positive
    private Integer walletOwnerId;

    @NotNull
    private Integer companyId;

    @NotNull(message = "Ilość akcji jest wymagana")
    @Min(value = 1, message = "Ilość akcji musi być większa od 0")
    private Integer sharesAmount;

    @Positive(message = "Cena musi być dodatnia")
    @Digits(integer = 17, fraction = 2, message = "Cena może mieć maksymalnie dwa miejsca po przecinku")
    private BigDecimal sharePrice;

    private LocalDateTime subscriptionEnd;
}
