package pl.gpwpoid.origin.ui.views.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.gpwpoid.origin.models.account.Account;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletDTO {
    private Integer accountId;

    @NotBlank(message = "Nazwa nie może być pusta")
    @Size(min = 1, max = 128, message = "Nazwa musi mieć 1-128 znaków")
    private String walletName;
}
