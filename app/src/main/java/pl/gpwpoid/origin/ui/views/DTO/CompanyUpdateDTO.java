package pl.gpwpoid.origin.ui.views.DTO;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CompanyUpdateDTO {
    @NotNull
    @Positive
    private Integer companyId;

    @NotBlank(message = "Nazwa jest wymagana")
    @Size(max = 256, message = "Nazwa jest zbyt długa")
    private String companyName;

    @NotBlank(message = "Kod jest wymagany")
    @Size(min = 3, max = 3, message = "Kod musi mieć 3 znaki")
    @Pattern(regexp = "[A-Z]{3}", message = "Kod musi składać się z 3 wielkich liter")
    private String companyCode;

    @NotNull(message = "Miasto jest wymagane")
    private Integer townId;

    @NotBlank(message = "Kod pocztowy jest wymagany")
    private String postalCode;

    @Size(max = 128, message = "Ulica zbyt długa")
    private String street;

    @Size(max = 8, message = "Numer ulicy zbyt długi")
    private String streetNumber;

    @Size(max = 8, message = "Numer lokalu zbyt długi")
    private String apartmentNumber;
}
