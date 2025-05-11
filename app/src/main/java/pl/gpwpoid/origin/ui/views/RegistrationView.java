package pl.gpwpoid.origin.ui.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import pl.gpwpoid.origin.models.address.Town;
import pl.gpwpoid.origin.services.AccountService;
import pl.gpwpoid.origin.services.AddressService;
import pl.gpwpoid.origin.ui.views.DTO.RegistrationDTO;

import java.util.Collections;

@PageTitle("Rejestracja")
@Route("register")
@AnonymousAllowed
public class RegistrationView extends VerticalLayout {

    private final AccountService accountService;
    private final AddressService addressService;

    private Binder<RegistrationDTO> binder = new BeanValidationBinder<>(RegistrationDTO.class);
    private RegistrationDTO registrationDTO;

    // Pola formularza
    private EmailField email = new EmailField("Email");
    private PasswordField password = new PasswordField("Hasło");
    private PasswordField confirmPassword = new PasswordField("Potwierdź hasło");
    private TextField firstName = new TextField("Imię");
    private TextField secondaryName = new TextField("Drugie imię (opcjonalne)");
    private TextField lastName = new TextField("Nazwisko");

    private ComboBox<Town> townComboBox = new ComboBox<>("Miasto");
    private ComboBox<String> postalCodeComboBox = new ComboBox<>("Kod pocztowy");

    private TextField street = new TextField("Ulica");
    private TextField streetNumber = new TextField("Numer domu");
    private TextField apartmentNumber = new TextField("Numer mieszkania (opcjonalne)");
    private TextField phoneNumber = new TextField("Numer telefonu (np. +48123456789)");
    private TextField pesel = new TextField("PESEL");

    private Button registerButton = new Button("Zarejestruj");

    @Autowired
    public RegistrationView(AccountService accountService, AddressService addressService) {
        this.accountService = accountService;
        this.addressService = addressService;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        FormLayout formLayout = createRegistrationForm();
        add(new H2("Formularz Rejestracyjny"), formLayout);

        bindFields();
        configureAddressFields();
        configureRegisterButton();
    }

    private FormLayout createRegistrationForm() {
        FormLayout formLayout = new FormLayout();
        formLayout.add(
                email, password,
                confirmPassword, firstName,
                secondaryName, lastName,
                townComboBox, postalCodeComboBox,
                street, streetNumber,
                apartmentNumber, phoneNumber,
                pesel, registerButton
        );
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );
        formLayout.setWidth("700px"); // Możesz dostosować szerokość
        formLayout.setColspan(registerButton, 2);
        return formLayout;
    }

    private void bindFields() {
        this.registrationDTO = new RegistrationDTO();
        binder.setBean(registrationDTO);

        binder.forField(email).bind("email");
        binder.forField(password).bind("password");
        binder.forField(confirmPassword)
                .withValidator(confirm -> confirm.equals(password.getValue()), "Hasła muszą być takie same")
                .bind("confirmPassword");
        binder.forField(firstName).bind("firstName");
        binder.forField(secondaryName).bind("secondaryName");
        binder.forField(lastName).bind("lastName");

        // Bindowanie ComboBoxów do DTO
        binder.forField(townComboBox)
                .asRequired("Miasto jest wymagane") // Dodatkowa walidacja, bo DTO ma townId
                .bind(dto -> dto.getTownId() != null ? addressService.getTownById(dto.getTownId()).orElse(null) : null,
                        (dto, town) -> dto.setTownId(town != null ? town.getTownId() : null));

        binder.forField(postalCodeComboBox)
                .asRequired("Kod pocztowy jest wymagany") // Dodatkowa walidacja
                .bind("postalCode");

        binder.forField(street).bind("street");
        binder.forField(streetNumber).bind("streetNumber");
        binder.forField(apartmentNumber).bind("apartmentNumber");
        binder.forField(phoneNumber).bind("phoneNumber");
        binder.forField(pesel).bind("pesel");

        // Wypełnij DTO pustym obiektem na start
        binder.setBean(new RegistrationDTO());
    }

    private void configureAddressFields() {
        // Konfiguracja ComboBoxa dla miast
        townComboBox.setItems(query -> {
            String name = query.getFilter().orElse("");
            return addressService.getTownsByName(name, query.getOffset(), query.getLimit());
        }, query -> {
            String name = query.getFilter().orElse("");
            return addressService.countTownsByName(name);
        });
        townComboBox.setItemLabelGenerator(Town::getName);
        townComboBox.setPlaceholder("Wpisz nazwę miasta...");
        townComboBox.setClearButtonVisible(true);
        townComboBox.setPageSize(15); // Liczba elementów na stronę sugestii

        // ComboBox dla kodów pocztowych - początkowo pusty i nieaktywny
        postalCodeComboBox.setItems(Collections.emptyList());
        postalCodeComboBox.setEnabled(false);
        postalCodeComboBox.setPlaceholder("Najpierw wybierz miasto");
        postalCodeComboBox.setClearButtonVisible(true);

        // Listener dla zmiany wartości w ComboBoxie miast
        townComboBox.addValueChangeListener(event -> {
            Town selectedTown = event.getValue();
            postalCodeComboBox.clear();
            if (selectedTown != null && selectedTown.getTownId() != null) {
                addressService.getTownById(selectedTown.getTownId()).ifPresent(fullTown -> { // findFullTownById zwraca Optional<Town>
                    postalCodeComboBox.setItems(addressService.getPostalCodesFromTown(fullTown).stream().map(
                            postalCodesTowns -> postalCodesTowns.getPostalCode().getPostalCode()
                    ).toList()); // Zakładając, że ta metoda zwraca List<String>
                    postalCodeComboBox.setEnabled(true);
                    postalCodeComboBox.setPlaceholder("Wybierz kod pocztowy");
                });
            } else {
                postalCodeComboBox.setItems(Collections.emptyList());
                postalCodeComboBox.setEnabled(false);
                postalCodeComboBox.setPlaceholder("Najpierw wybierz miasto");
            }
            // Wyczyść bindowanie kodu pocztowego, aby uniknąć problemów z walidacją
            // gdy miasto się zmienia i stary kod pocztowy przestaje być ważny
            if (binder.getBean() != null) { // Upewnij się, że bean jest ustawiony
                binder.getBean().setPostalCode(null);
            }
        });
    }

    private void configureRegisterButton() {
        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerButton.addClickListener(event -> {
            try {
                // Walidacja i pobranie danych z bindera
                binder.writeBean(registrationDTO);

                accountService.addAccount(registrationDTO);
                Notification.show("Rejestracja zakończona sukcesem!", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                // Można wyczyścić formularz lub przekierować użytkownika
                this.registrationDTO = new RegistrationDTO();
                binder.setBean(this.registrationDTO); // Reset formularza
                townComboBox.clear(); // Również wyczyść pola adresowe
                // getUI().ifPresent(ui -> ui.navigate("login")); // Przekierowanie na logowanie
            } catch (ValidationException e) {
                Notification.show("Proszę poprawić błędy w formularzu.", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (Exception e) {
                // Tutaj możesz chcieć bardziej szczegółowo obsłużyć wyjątki, np. gdy email/pesel już istnieje
                Notification.show("Wystąpił błąd podczas rejestracji: " + e.getMessage(), 5000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
    }
}
