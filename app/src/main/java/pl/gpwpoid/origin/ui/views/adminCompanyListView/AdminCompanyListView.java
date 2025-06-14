package pl.gpwpoid.origin.ui.views.adminCompanyListView;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import pl.gpwpoid.origin.services.AddressService;
import pl.gpwpoid.origin.services.CompanyService;
import pl.gpwpoid.origin.services.IPOService;
import pl.gpwpoid.origin.ui.views.MainLayout;

@Route(value = "admin/companies", layout = MainLayout.class)
@PageTitle("Lista Firm")
@RolesAllowed("admin")
public class AdminCompanyListView extends VerticalLayout {
    private final AddressService addressService;
    private final CompanyService companyService;
    private final IPOService ipoService;

    public AdminCompanyListView(CompanyService companyService, AddressService addressService, IPOService ipoService) {
        this.companyService = companyService;
        this.addressService = addressService;
        this.ipoService = ipoService;

        setSizeFull();
        setSpacing(true);
        setPadding(true);

        AdminCompaniesGrid companiesGrid = new AdminCompaniesGrid(companyService, ipoService);
        AdminCompanyCreationForm creationForm = new AdminCompanyCreationForm(companyService, addressService, companiesGrid);
        add(companiesGrid, creationForm);
        companiesGrid.updateList();
    }
}
