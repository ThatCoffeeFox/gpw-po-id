package pl.gpwpoid.origin.ui.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import pl.gpwpoid.origin.models.account.Account;
import pl.gpwpoid.origin.models.account.AccountInfo;
import pl.gpwpoid.origin.models.address.Town;
import pl.gpwpoid.origin.repositories.views.AccountAuthItem;
import pl.gpwpoid.origin.services.AccountService;
import pl.gpwpoid.origin.services.AddressService;
import pl.gpwpoid.origin.ui.views.DTO.ProfileUpdateDTO;
import pl.gpwpoid.origin.utils.ExtendedUserDetails;
import pl.gpwpoid.origin.utils.SecurityUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;

@Route(value = "profile", layout = MainLayout.class)
@PageTitle("Mój Profil")
@PermitAll
public class ProfileView extends VerticalLayout {

    private final AccountService accountService;
    private final AddressService addressService;

    private final Binder<ProfileUpdateDTO> binder = new Binder<>(ProfileUpdateDTO.class);
    private ProfileUpdateDTO currentProfileData = new ProfileUpdateDTO();


    private final TextField firstNameField = new TextField("Imię");
    private final TextField secondaryNameField = new TextField("Drugie imię (opcjonalnie)");
    private final TextField lastNameField = new TextField("Nazwisko");
    private final TextField emailField = new TextField("Email");
    private final ComboBox<Town> townComboBox = new ComboBox<>("Miasto");
    private final ComboBox<String> postalCodeComboBox = new ComboBox<>("Kod pocztowy");
    private final TextField streetField = new TextField("Ulica");
    private final TextField streetNumberField = new TextField("Numer ulicy");
    private final TextField apartmentNumberField = new TextField("Numer mieszkania (opcjonalnie)");
    private final TextField phoneNumberField = new TextField("Numer telefonu");
    private final TextField peselField = new TextField("PESEL");

    private final Button saveButton = new Button("Zapisz zmiany");
    private final Button cancelButton = new Button("Anuluj");

    @Autowired
    public ProfileView(AccountService accountService, AddressService addressService) {
        this.accountService = accountService;
        this.addressService = addressService;


        configureForm();
        setupBinder();
        loadUserData();


        add(new H2("Edytuj dane profilu"), createFormLayout(), createButtonLayout());

        setSizeFull();
        setAlignItems(Alignment.CENTER);
    }

    private void configureForm() {
        firstNameField.setReadOnly(true);
        secondaryNameField.setReadOnly(true);
        lastNameField.setReadOnly(true);


        townComboBox.setItems(query ->
                addressService.getTownsByName(
                        query.getFilter().orElse(""),
                        query.getOffset(),
                        query.getLimit()
                )
        );
        townComboBox.setItemLabelGenerator(Town::getName);
        townComboBox.setPlaceholder("Wybierz lub wpisz miasto");
        townComboBox.setClearButtonVisible(true);


        postalCodeComboBox.setItems(Collections.emptyList());
        postalCodeComboBox.setEnabled(false);
        postalCodeComboBox.setPlaceholder("Najpierw wybierz miasto");
        postalCodeComboBox.setClearButtonVisible(true);


        townComboBox.addValueChangeListener(event -> {
            Town selectedTown = event.getValue();
            postalCodeComboBox.clear();

            if (selectedTown != null && selectedTown.getTownId() != null) {


                postalCodeComboBox.setItems(addressService.getPostalCodesFromTown(selectedTown).stream()
                        .map(postalCodesTowns -> postalCodesTowns.getPostalCode().getPostalCode())
                        .distinct()
                        .sorted()
                        .toList());
                postalCodeComboBox.setEnabled(true);
                postalCodeComboBox.setPlaceholder("Wybierz kod pocztowy");
            } else {
                postalCodeComboBox.setItems(Collections.emptyList());
                postalCodeComboBox.setEnabled(false);
                postalCodeComboBox.setPlaceholder("Najpierw wybierz miasto");
            }


            if (binder.getBean() != null) {
                binder.getBean().setPostalCode(null);
            }
        });

        emailField.setRequiredIndicatorVisible(true);
        townComboBox.setRequiredIndicatorVisible(true);

        postalCodeComboBox.setRequiredIndicatorVisible(true);
        phoneNumberField.setRequiredIndicatorVisible(true);
        peselField.setRequiredIndicatorVisible(true);
    }

    private FormLayout createFormLayout() {
        FormLayout formLayout = new FormLayout();
        formLayout.add(
                firstNameField, lastNameField,
                secondaryNameField, emailField,
                townComboBox, postalCodeComboBox,
                streetField, streetNumberField,
                apartmentNumberField, phoneNumberField,
                peselField
        );
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2)
        );
        formLayout.setMaxWidth("800px");
        return formLayout;
    }

    private HorizontalLayout createButtonLayout() {
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> saveProfile());
        cancelButton.addClickListener(e -> loadUserData());

        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);
        buttonLayout.setSpacing(true);
        return buttonLayout;
    }

    private void setupBinder() {
        binder.forField(emailField)
                .asRequired("Email jest wymagany.")
                .withValidator(new EmailValidator("Niepoprawny format adresu email."))
                .bind(ProfileUpdateDTO::getEmail, ProfileUpdateDTO::setEmail);

        binder.forField(townComboBox)
                .asRequired("Miasto jest wymagane.")
                .bind(
                        dto -> {
                            if (dto.getTownId() == null) return null;
                            return addressService.getTownById(dto.getTownId()).orElse(null);
                        },
                        (dto, town) -> {
                            dto.setTownId(town != null ? town.getTownId() : null);
                        }
                );

        binder.forField(postalCodeComboBox)
                .asRequired("Kod pocztowy jest wymagany.")
                .withValidator(pc -> pc == null || pc.matches("\\d{2}-\\d{3}"), "Niepoprawny format kodu pocztowego (np. 00-000).")
                .bind(ProfileUpdateDTO::getPostalCode, ProfileUpdateDTO::setPostalCode);

        binder.forField(streetField)
                .bind(ProfileUpdateDTO::getStreet, ProfileUpdateDTO::setStreet);
        binder.forField(streetNumberField)
                .bind(ProfileUpdateDTO::getStreetNumber, ProfileUpdateDTO::setStreetNumber);
        binder.forField(apartmentNumberField)
                .bind(ProfileUpdateDTO::getApartmentNumber, ProfileUpdateDTO::setApartmentNumber);

        binder.forField(phoneNumberField)
                .asRequired("Numer telefonu jest wymagany.")

                .withValidator(pn -> pn == null || pn.matches("\\+\\d{10,13}"), "Niepoprawny format numeru telefonu (np. +48123456789).")
                .bind(ProfileUpdateDTO::getPhoneNumber, ProfileUpdateDTO::setPhoneNumber);

        binder.forField(peselField)
                .asRequired("PESEL jest wymagany.")

                .withValidator(p -> p == null || p.matches("\\d{11}"), "PESEL musi składać się z 11 cyfr.")
                .bind(ProfileUpdateDTO::getPesel, ProfileUpdateDTO::setPesel);


        binder.setBean(currentProfileData);
    }

    private void loadUserData() {
        System.out.println("Loading user data...");
        Optional<ExtendedUserDetails> authUserOpt = SecurityUtils.getAuthenticatedUser();
        if (authUserOpt.isEmpty()) {

            return;
        }

        Integer accountId = authUserOpt.get().getAccountId();
        AccountInfo latestInfo = accountService.getNewestAccountInfoItemById(accountId);

        if (latestInfo == null) {

            return;
        }

        firstNameField.setValue(latestInfo.getFirstName());
        secondaryNameField.setValue(latestInfo.getSecondaryName() != null ? latestInfo.getSecondaryName() : "");
        lastNameField.setValue(latestInfo.getLastName());

        currentProfileData.setAccountId(accountId);
        currentProfileData.setEmail(latestInfo.getEmail());

        Town currentTown = null;
        if (latestInfo.getPostalCodesTowns() != null && latestInfo.getPostalCodesTowns().getTown() != null) {
            currentTown = latestInfo.getPostalCodesTowns().getTown();
            currentProfileData.setTownId(currentTown.getTownId());
            townComboBox.setValue(currentTown);
        } else {
            townComboBox.clear();
        }


        postalCodeComboBox.clear();
        if (currentTown != null) {
            postalCodeComboBox.setItems(addressService.getPostalCodesFromTown(currentTown).stream()
                    .map(postalCodesTowns -> postalCodesTowns.getPostalCode().getPostalCode())
                    .distinct()
                    .sorted()
                    .toList());
            postalCodeComboBox.setEnabled(true);
            postalCodeComboBox.setPlaceholder("Wybierz kod pocztowy");


            if (latestInfo.getPostalCodesTowns() != null && latestInfo.getPostalCodesTowns().getPostalCode() != null) {
                String currentPostalCode = latestInfo.getPostalCodesTowns().getPostalCode().getPostalCode();
                currentProfileData.setPostalCode(currentPostalCode);
                postalCodeComboBox.setValue(currentPostalCode);
            }
        } else {
            postalCodeComboBox.setItems(Collections.emptyList());
            postalCodeComboBox.setEnabled(false);
            postalCodeComboBox.setPlaceholder("Najpierw wybierz miasto");
        }

        currentProfileData.setStreet(latestInfo.getStreet());
        currentProfileData.setStreetNumber(latestInfo.getStreetNumber());
        currentProfileData.setApartmentNumber(latestInfo.getApartmentNumber());
        currentProfileData.setPhoneNumber(latestInfo.getPhoneNumber());
        currentProfileData.setPesel(latestInfo.getPesel());

        binder.setBean(currentProfileData);
    }

    private void saveProfile() {
        if (binder.validate().isOk()) {
            try {

                binder.writeBean(currentProfileData);
                accountService.updateAccount(currentProfileData);
                Notification.show("Dane profilu zostały pomyślnie zaktualizowane.", 3000, Notification.Position.MIDDLE);
                loadUserData();
            } catch (ValidationException e) {


                Notification.show("Błąd walidacji formularza.", 3000, Notification.Position.MIDDLE);
            } catch (IllegalArgumentException | NullPointerException e) {
                Notification.show("Błąd zapisu danych: " + e.getMessage(), 4000, Notification.Position.MIDDLE);
            } catch (Exception e) {
                Notification.show("Wystąpił nieoczekiwany błąd podczas zapisu: " + e.getMessage(), 5000, Notification.Position.MIDDLE);

                e.printStackTrace();
            }
        } else {
            Notification.show("Proszę poprawić błędy w formularzu.", 3000, Notification.Position.MIDDLE);
        }
    }
}