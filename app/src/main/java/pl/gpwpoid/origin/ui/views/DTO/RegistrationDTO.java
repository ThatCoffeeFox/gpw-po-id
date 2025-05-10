package pl.gpwpoid.origin.ui.views.DTO;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.gpwpoid.origin.decorators.validPesel.ValidPesel;
import pl.gpwpoid.origin.models.account.Account;

@Data
@NoArgsConstructor
public class RegistrationDTO {

    @NotBlank(message = "Email jest wymagany")
    @Email(message = "Niepoprawny format email")
    @Size(max = 256, message = "Email zbyt długi")
    private String email;

    @NotBlank(message = "Hasło jest wymagane")
    @Size(min = 8, message = "Hasło musi mieć co najmniej 8 znaków")
    private String password;

    @NotBlank(message = "Potwierdzenie hasła jest wymagane")
    private String confirmPassword;

    @NotBlank(message = "Imię jest wymagane")
    @Size(max = 128, message = "Imię zbyt długie")
    private String firstName;

    @Size(max = 128, message = "Drugie imię zbyt długie")
    private String secondaryName;

    @NotBlank(message = "Nazwisko jest wymagane")
    @Size(max = 256, message = "Nazwisko zbyt długie")
    private String lastName;

    @NotNull(message = "Miasto jest wymagane")
    private Integer townId; // Będziemy przechowywać ID wybranego miasta

    @NotBlank(message = "Kod pocztowy jest wymagany")
    private String postalCode; // Będziemy przechowywać wybrany kod pocztowy

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
    @ValidPesel // Twoja niestandardowa adnotacja walidacyjna
    private String pesel;

    // Domyślnie rola użytkownika to 'user'
    private Account.UserRole role = Account.UserRole.user;
}
