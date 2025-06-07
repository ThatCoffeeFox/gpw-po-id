package pl.gpwpoid.origin.ui.views.AdminCompanyView;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import pl.gpwpoid.origin.models.address.Town;
import pl.gpwpoid.origin.models.company.CompanyInfo;
import pl.gpwpoid.origin.repositories.views.CompanyStatusItem;
import pl.gpwpoid.origin.services.AddressService;
import pl.gpwpoid.origin.services.CompanyService;
import pl.gpwpoid.origin.ui.views.DTO.CompanyUpdateDTO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collections;
import java.util.Locale;

public class AdminChangeCompanyInfoForm extends VerticalLayout {
    private final AddressService addressService;
    private final CompanyService companyService;
    private Integer companyId;

    private final Binder<CompanyUpdateDTO> binder = new BeanValidationBinder<>(CompanyUpdateDTO.class);
    private CompanyUpdateDTO currentCompanyData = new CompanyUpdateDTO();

    private final TextField name = new TextField("Nazwa Firmy");
    private final TextField code = new TextField("Kod firmy");
    private final ComboBox<Town> town = new ComboBox<>("Miasto");
    private final ComboBox<String> postalCode = new ComboBox<>("Kod pocztowy");
    private final TextField street = new TextField("Ulica");
    private final TextField streetNumber = new TextField("Numer budynku");
    private final TextField apartmentNumber = new TextField("Numer lokalu");

    private final TextField sharePrice = new TextField("Cena za akcję");
    private final TextField changeInPrice = new TextField("Zmiana ceny");
    private final TextField tradable = new TextField("Tradable");

    private final Button saveButton = new Button("Zatwierdź");
    private final Button cancelButton = new Button("Anuluj");
    private final Button navigateToIPOButton = new Button("Rozpocznij IPO");

    private static final DecimalFormat FUNDS_FORMATTER = new DecimalFormat(
            "#,##0.00",
            DecimalFormatSymbols.getInstance(new Locale("pl", "PL"))
    );

    public AdminChangeCompanyInfoForm(AddressService addressService, CompanyService companyService) {
        this.addressService = addressService;
        this.companyService = companyService;

        add(new H3("Edytuj dane firmy"));

        bindFields();
        HorizontalLayout layout = new HorizontalLayout(configureForm(), configureCompanyStatus());
        add(layout);
    }

    private VerticalLayout configureCompanyStatus(){
        VerticalLayout companyStatusLayout = new VerticalLayout();
        companyStatusLayout.add(sharePrice, changeInPrice, tradable);
        companyStatusLayout.setPadding(true);
        companyStatusLayout.setSpacing(true);
        sharePrice.setReadOnly(true);
        changeInPrice.setReadOnly(true);
        tradable.setReadOnly(true);

        return companyStatusLayout;
    }

    private void bindFields() {
        binder.forField(name)
                .asRequired("Nazwa jest wymagana")
                .bind(CompanyUpdateDTO::getCompanyName, CompanyUpdateDTO::setCompanyName);
        binder.forField(code)
                .asRequired("Kod firmy jest wymagany")
                .bind(CompanyUpdateDTO::getCompanyCode, CompanyUpdateDTO::setCompanyCode);
        binder.forField(town)
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

        binder.forField(postalCode)
                .asRequired("Kod pocztowy jest wymagany.")
                .withValidator(pc -> pc == null || pc.matches("\\d{2}-\\d{3}"), "Niepoprawny format kodu pocztowego (np. 00-000).")
                .bind(CompanyUpdateDTO::getPostalCode, CompanyUpdateDTO::setPostalCode);

        binder.forField(street)
                .bind(CompanyUpdateDTO::getStreet, CompanyUpdateDTO::setStreet);
        binder.forField(streetNumber)
                .bind(CompanyUpdateDTO::getStreetNumber, CompanyUpdateDTO::setStreetNumber);
        binder.forField(apartmentNumber)
                .bind(CompanyUpdateDTO::getApartmentNumber, CompanyUpdateDTO::setApartmentNumber);

        binder.setBean(currentCompanyData);
    }

    private VerticalLayout configureForm() {
        FormLayout formLayout = new FormLayout();
        formLayout.add(name, code, town, postalCode, street, streetNumber, apartmentNumber);
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2)
        );
        formLayout.setMaxWidth("1200px");
        configureFormFields();

        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        saveButton.addClickListener(e->saveChanges());
        cancelButton.addClickListener(e->updateCompanyData());
        buttonLayout.setSpacing(true);

        return new VerticalLayout(formLayout, buttonLayout);
    }

    private void configureFormFields() {
        name.setRequiredIndicatorVisible(true);
        code.setRequiredIndicatorVisible(true);
        town.setRequiredIndicatorVisible(true);
        postalCode.setRequiredIndicatorVisible(true);
        town.setItems(query ->
                addressService.getTownsByName(
                        query.getFilter().orElse(""),
                        query.getOffset(),
                        query.getLimit()
                )
        );
        town.setItemLabelGenerator(Town::getName);
        town.setPlaceholder("Wybierz lub wpisz miasto");
        town.setClearButtonVisible(true);


        postalCode.setItems(Collections.emptyList());
        postalCode.setEnabled(false);
        postalCode.setPlaceholder("Najpierw wybierz miasto");
        postalCode.setClearButtonVisible(true);


        town.addValueChangeListener(event -> {
            Town selectedTown = event.getValue();
            postalCode.clear();

            if (selectedTown != null && selectedTown.getTownId() != null) {


                postalCode.setItems(addressService.getPostalCodesFromTown(selectedTown).stream()
                        .map(postalCodesTowns -> postalCodesTowns.getPostalCode().getPostalCode())
                        .distinct()
                        .sorted()
                        .toList());
                postalCode.setEnabled(true);
                postalCode.setPlaceholder("Wybierz kod pocztowy");
            } else {
                postalCode.setItems(Collections.emptyList());
                postalCode.setEnabled(false);
                postalCode.setPlaceholder("Najpierw wybierz miasto");
            }


            if (binder.getBean() != null) {
                binder.getBean().setPostalCode(null);
            }
        });
    }

    private void saveChanges() {
        if(binder.validate().isOk()) {
            try {
                binder.writeBean(currentCompanyData);
                companyService.updateCompany(currentCompanyData);
                Notification.show("Dane firmy zostały zaktualizowane", 4000, Notification.Position.TOP_CENTER);
                updateCompanyData();
            } catch (ValidationException e){
                Notification.show("Błąd walidacji", 4000, Notification.Position.TOP_CENTER);
            } catch (Exception e){
                Notification.show("Wystąpił błąd: " + e.getMessage(), 4000, Notification.Position.TOP_CENTER);
            }
        }
        else
            Notification.show("Proszę wpisać poprawne dane", 4000, Notification.Position.TOP_CENTER);
    }

    public void setCompany(Integer companyId) {
        this.companyId = companyId;
    }

    public void updateCompanyData(){
        CompanyInfo latestInfo = companyService.getNewestCompanyInfoItemById(companyId);

        if(latestInfo == null)
            return;

        name.setValue(latestInfo.getName());
        code.setValue(latestInfo.getCode());

        currentCompanyData.setCompanyId(companyId);

        Town currentTown = null;
        if (latestInfo.getPostalCodesTowns() != null && latestInfo.getPostalCodesTowns().getTown() != null) {
            currentTown = latestInfo.getPostalCodesTowns().getTown();
            currentCompanyData.setTownId(currentTown.getTownId());
            town.setValue(currentTown);
        } else {
            town.clear();
        }

        postalCode.clear();
        if (currentTown != null) {
            postalCode.setItems(addressService.getPostalCodesFromTown(currentTown).stream()
                    .map(postalCodesTowns -> postalCodesTowns.getPostalCode().getPostalCode())
                    .distinct()
                    .sorted()
                    .toList());
            postalCode.setEnabled(true);
            postalCode.setPlaceholder("Wybierz kod pocztowy");

            if (latestInfo.getPostalCodesTowns() != null && latestInfo.getPostalCodesTowns().getPostalCode() != null) {
                String currentPostalCode = latestInfo.getPostalCodesTowns().getPostalCode().getPostalCode();
                currentCompanyData.setPostalCode(currentPostalCode);
                postalCode.setValue(currentPostalCode);
            }
        } else {
            postalCode.setItems(Collections.emptyList());
            postalCode.setEnabled(false);
            postalCode.setPlaceholder("Najpierw wybierz miasto");
        }
        currentCompanyData.setStreet(latestInfo.getStreet());
        street.setValue(latestInfo.getStreet());

        currentCompanyData.setStreetNumber(latestInfo.getStreetNumber());
        streetNumber.setValue(latestInfo.getStreetNumber());

        currentCompanyData.setApartmentNumber(latestInfo.getApartmentNumber());
        apartmentNumber.setValue(latestInfo.getApartmentNumber());

        CompanyStatusItem companyStatusItem = companyService.getCompanyStatusItemById(companyId);

        if(companyStatusItem != null) {
            sharePrice.setValue(formatCurrentPrice(companyStatusItem));
            changeInPrice.setValue(formatPercentage(companyStatusItem));
            tradable.setValue(translateTradable(companyStatusItem));
        }
        else{
            Notification.show("null", 10000, Notification.Position.TOP_CENTER);
        }
    }

    private String formatCurrentPrice(CompanyStatusItem item){
        if(item.getCurrentPrice() == null)
            return "brak";
        else
            return FUNDS_FORMATTER.format(item.getCurrentPrice()) + " zł";
    }

    private String formatPercentage(CompanyStatusItem item) {
        if(item.getPreviousPrice() == null)
            return "0%";
        BigDecimal percentage = item.getCurrentPrice().divide(item.getPreviousPrice(), 10, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP).add(new BigDecimal("-100"));
        return percentage + "%";
    }

    private String translateTradable(CompanyStatusItem item) {
        if(item.getTradable())
            return "Tak";
        return "Nie";
    }
}
