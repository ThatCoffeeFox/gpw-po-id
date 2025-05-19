package pl.gpwpoid.origin.ui.views.DTO;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.gpwpoid.origin.models.company.Company;
import pl.gpwpoid.origin.models.order.OrderType;
import pl.gpwpoid.origin.repositories.views.WalletListItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class OrderDTO {
    @NotNull(message = "Typ zlecenia jest wymagany")
    private String orderType;

    @NotNull(message = "Portfel jest wymagany")
    private Integer walletId;

    @NotNull(message = "Ilość akcji jest wymagana")
    @Min(value = 1, message = "Ilość akcji musi być większa od 0")
    private Integer sharesAmount;

    @NotNull
    private Integer companyId;

    @Positive(message = "Cena musi być dodatnia")
    @Digits(integer = 17, fraction = 2, message = "Cena może mieć maksymalnie dwa miejsca po przecinku")
    private BigDecimal sharePrice;

    @Future(message = "Data wygaśnięcia nie może być w przeszłości")
    private LocalDateTime orderExpirationDate;
}
