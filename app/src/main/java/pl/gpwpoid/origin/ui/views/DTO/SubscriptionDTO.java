package pl.gpwpoid.origin.ui.views.DTO;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
public class SubscriptionDTO {
    @NonNull
    private Integer ipoId;

    @NonNull
    private Integer walletId;

    @NonNull
    @Positive(message = "Liczba akcji musoi być dodatnia")
    private Integer sharesAmount;
}
