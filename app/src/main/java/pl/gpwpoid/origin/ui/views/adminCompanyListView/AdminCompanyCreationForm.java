package pl.gpwpoid.origin.ui.views.adminCompanyListView;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import pl.gpwpoid.origin.models.address.Town;
import pl.gpwpoid.origin.services.AddressService;
import pl.gpwpoid.origin.services.CompanyService;
import pl.gpwpoid.origin.ui.views.DTO.CompanyDTO;

import java.util.Collections;

public class AdminCompanyCreationForm extends VerticalLayout {
    private final CompanyService companyService;
    private final AddressService addressService;
    private final AdminCompaniesGrid companiesGird;

    private final Binder<CompanyDTO> binder = new BeanValidationBinder<>(CompanyDTO.class);
    private CompanyDTO companyDTO;

    private final TextField name = new TextField("Nazwa Firmy");
    private final TextField code = new TextField("Kod firmy");
    private final ComboBox<Town> town = new ComboBox<>("Miasto");
    private final ComboBox<String> postalCode = new ComboBox<>("Kod pocztowy");
    private final TextField street = new TextField("Ulica");
    private final TextField streetNumber = new TextField("Numer budynku");
    private final TextField apartmentNumber = new TextField("Numer lokalu");

    private final Button createCompanyButton = new Button("Zatwierdź");

    public AdminCompanyCreationForm(CompanyService companyService, AddressService addressService, AdminCompaniesGrid companiesGrid) {
        this.companyService = companyService;
        this.addressService = addressService;
        this.companiesGird = companiesGrid;

        FormLayout formLayout = createCompanyForm();
        add(new H3("Dodaj nową firmę"), formLayout);

        bindFields();
        configureAddressFields();
        configureCreateCompanyButton();
    }

    private FormLayout createCompanyForm() {
        FormLayout formLayout = new FormLayout();
        formLayout.add(name, code, town, postalCode, street, streetNumber, apartmentNumber, createCompanyButton);
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );
        formLayout.setColspan(createCompanyButton, 2);
        return formLayout;
    }

    private void bindFields(){
        binder.forField(name).bind("companyName");
        binder.forField(code).bind("companyCode");
        binder.forField(town)
                .asRequired("Miasto jest wymagane")
                .bind(dto -> dto.getTownId() != null ? addressService.getTownById(dto.getTownId()).orElse(null) : null,
                        (dto, town) -> dto.setTownId(town != null ? town.getTownId() : null));

        binder.forField(postalCode)
                .asRequired("Kod pocztowy jest wymagany")
                .bind("postalCode");

        binder.forField(street).bind("street");
        binder.forField(streetNumber).bind("streetNumber");
        binder.forField(apartmentNumber).bind("apartmentNumber");

        this.companyDTO = new CompanyDTO();
        binder.setBean(companyDTO);
    }

    private void configureAddressFields() {

        town.setItems(query -> {
            String name = query.getFilter().orElse("");
            return addressService.getTownsByName(name, query.getOffset(), query.getLimit());
        }, query -> {
            String name = query.getFilter().orElse("");
            return addressService.countTownsByName(name);
        });
        town.setItemLabelGenerator(Town::getName);
        town.setPlaceholder("Wpisz nazwę miasta...");
        town.setClearButtonVisible(true);
        town.setPageSize(15);


        postalCode.setItems(Collections.emptyList());
        postalCode.setEnabled(false);
        postalCode.setPlaceholder("Najpierw wybierz miasto");
        postalCode.setClearButtonVisible(true);

        town.addValueChangeListener(event -> {
            Town selectedTown = event.getValue();
            postalCode.clear();
            if (selectedTown != null && selectedTown.getTownId() != null) {
                addressService.getTownById(selectedTown.getTownId()).ifPresent(fullTown -> {
                    postalCode.setItems(addressService.getPostalCodesFromTown(fullTown).stream().map(
                            postalCodesTowns -> postalCodesTowns.getPostalCode().getPostalCode()
                    ).toList());
                    postalCode.setEnabled(true);
                    postalCode.setPlaceholder("Wybierz kod pocztowy");
                });
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

    private void configureCreateCompanyButton(){
        createCompanyButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createCompanyButton.addClickListener(event -> {
            try{
                binder.writeBean(companyDTO);

                companyService.addCompany(companyDTO);
                Notification.show("Dodano firmę");

                companyDTO = new CompanyDTO();
                binder.setBean(companyDTO);
                town.clear();
                companiesGird.updateList();
            } catch (ValidationException e){
                Notification.show("Niepoprawne dane");
            } catch (Exception e){
                Notification.show("Wystąpił błąd" + e.getMessage());
            }
        });
    }
}
