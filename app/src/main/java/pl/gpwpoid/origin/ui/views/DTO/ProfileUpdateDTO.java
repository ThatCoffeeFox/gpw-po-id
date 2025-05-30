package pl.gpwpoid.origin.ui.views.DTO;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.gpwpoid.origin.decorators.validPesel.ValidPesel;

@Data
@NoArgsConstructor
public class ProfileUpdateDTO {
    @NotBlank
    private Integer accountId;

    @NotBlank
    @Email
    @Size(max = 256, message = "Email zbyt długi")
    private String email;

    @NotNull(message = "Miasto jest wymagane")
    private Integer townId;

    @NotBlank(message = "Kod pocztowy jest wymagany")
    private String postalCode;

    @Size(max = 128, message = "Ulica zbyt długa")
    private String street;

    @Size(max = 8, message = "Numer ulicy zbyt długi")
    private String streetNumber;

    @Size(max = 8, message = "Numer mieszkania zbyt długi")
    private String apartmentNumber;

    @NotBlank(message = "Numer telefonu jest wymagany")
    @Pattern(regexp = "\\+[0-9]{10,13}", message = "Niepoprawny format numeru telefonu (np. +48123456789)")
    private String phoneNumber;

    @NotBlank(message = "PESEL jest wymagany")
    @ValidPesel
    private String pesel;
}
