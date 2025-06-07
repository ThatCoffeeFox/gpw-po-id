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

    private final Binder<RegistrationDTO> binder = new BeanValidationBinder<>(RegistrationDTO.class);
    private RegistrationDTO registrationDTO;


    private final EmailField email = new EmailField("Email");
    private final PasswordField password = new PasswordField("Hasło");
    private final PasswordField confirmPassword = new PasswordField("Potwierdź hasło");
    private final TextField firstName = new TextField("Imię");
    private final TextField secondaryName = new TextField("Drugie imię (opcjonalne)");
    private final TextField lastName = new TextField("Nazwisko");

    private final ComboBox<Town> townComboBox = new ComboBox<>("Miasto");
    private final ComboBox<String> postalCodeComboBox = new ComboBox<>("Kod pocztowy");

    private final TextField street = new TextField("Ulica");
    private final TextField streetNumber = new TextField("Numer domu");
    private final TextField apartmentNumber = new TextField("Numer mieszkania (opcjonalne)");
    private final TextField phoneNumber = new TextField("Numer telefonu (np. +48123456789)");
    private final TextField pesel = new TextField("PESEL");

    private final Button registerButton = new Button("Zarejestruj");

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
        formLayout.setWidth("700px");
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


        binder.forField(townComboBox)
                .asRequired("Miasto jest wymagane")
                .bind(dto -> dto.getTownId() != null ? addressService.getTownById(dto.getTownId()).orElse(null) : null,
                        (dto, town) -> dto.setTownId(town != null ? town.getTownId() : null));

        binder.forField(postalCodeComboBox)
                .asRequired("Kod pocztowy jest wymagany")
                .bind("postalCode");

        binder.forField(street).bind("street");
        binder.forField(streetNumber).bind("streetNumber");
        binder.forField(apartmentNumber).bind("apartmentNumber");
        binder.forField(phoneNumber).bind("phoneNumber");
        binder.forField(pesel).bind("pesel");


        binder.setBean(new RegistrationDTO());
    }

    private void configureAddressFields() {

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
        townComboBox.setPageSize(15);


        postalCodeComboBox.setItems(Collections.emptyList());
        postalCodeComboBox.setEnabled(false);
        postalCodeComboBox.setPlaceholder("Najpierw wybierz miasto");
        postalCodeComboBox.setClearButtonVisible(true);


        townComboBox.addValueChangeListener(event -> {
            Town selectedTown = event.getValue();
            postalCodeComboBox.clear();
            if (selectedTown != null && selectedTown.getTownId() != null) {
                addressService.getTownById(selectedTown.getTownId()).ifPresent(fullTown -> {
                    postalCodeComboBox.setItems(addressService.getPostalCodesFromTown(fullTown).stream().map(
                            postalCodesTowns -> postalCodesTowns.getPostalCode().getPostalCode()
                    ).toList());
                    postalCodeComboBox.setEnabled(true);
                    postalCodeComboBox.setPlaceholder("Wybierz kod pocztowy");
                });
            } else {
                postalCodeComboBox.setItems(Collections.emptyList());
                postalCodeComboBox.setEnabled(false);
                postalCodeComboBox.setPlaceholder("Najpierw wybierz miasto");
            }


            if (binder.getBean() != null) {
                binder.getBean().setPostalCode(null);
            }
        });
    }

    private void configureRegisterButton() {
        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerButton.addClickListener(event -> {
            try {

                binder.writeBean(registrationDTO);

                accountService.addAccount(registrationDTO);
                Notification.show("Rejestracja zakończona sukcesem!", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                this.registrationDTO = new RegistrationDTO();
                binder.setBean(this.registrationDTO);
                townComboBox.clear();
                getUI().ifPresent(ui -> ui.navigate("login"));
            } catch (ValidationException e) {
                Notification.show("Proszę poprawić błędy w formularzu.", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (Exception e) {

                Notification.show("Wystąpił błąd podczas rejestracji: " + e.getMessage(), 5000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
    }
}
