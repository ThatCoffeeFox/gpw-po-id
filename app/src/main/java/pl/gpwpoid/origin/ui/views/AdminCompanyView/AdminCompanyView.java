package pl.gpwpoid.origin.ui.views.AdminCompanyView;


import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import pl.gpwpoid.origin.services.*;
import pl.gpwpoid.origin.ui.views.MainLayout;

@Route(value = "/admin/companies", layout = MainLayout.class)
@RolesAllowed("admin")
public class AdminCompanyView extends VerticalLayout implements HasUrlParameter<Integer> {
    private final CompanyService companyService;
    private final IPOService ipoService;
    private final TransactionService transactionService;
    private final AddressService addressService;
    private final WalletsService walletService;
    private final AccountService accountService;

    private final AdminChangeCompanyInfoForm adminChangeCompanyInfoForm;
    private final AdminCompanyTransactionsGrid adminCompanyTransactionsGrid;
    private final AdminCompanyIPOsGrid adminCompanyIPOsGrid;
    private final AdminStartIPODialog adminStartIPODialog;

    private Integer companyId;

    @Autowired
    public AdminCompanyView(
            CompanyService companyService,
            IPOService ipoService,
            TransactionService transactionService,
            AddressService addressService,
            WalletsService walletService,
            AccountService accountService) {
        this.companyService = companyService;
        this.ipoService = ipoService;
        this.transactionService = transactionService;
        this.addressService = addressService;
        this.walletService = walletService;
        this.accountService = accountService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        adminChangeCompanyInfoForm = new AdminChangeCompanyInfoForm(addressService, companyService);
        adminChangeCompanyInfoForm.setWidth("100%");
        adminStartIPODialog = new AdminStartIPODialog(ipoService, accountService, walletService, companyService);
        adminStartIPODialog.setAlignItems(Alignment.END);
        HorizontalLayout infoLayout = new HorizontalLayout();
        infoLayout.add(new H3("Edytuj dane firmy"), adminStartIPODialog);
        infoLayout.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        infoLayout.setPadding(true);

        adminCompanyTransactionsGrid = new AdminCompanyTransactionsGrid(transactionService, companyService);
        adminCompanyIPOsGrid = new AdminCompanyIPOsGrid(ipoService);

        add(infoLayout, adminChangeCompanyInfoForm, adminCompanyTransactionsGrid, adminCompanyIPOsGrid);
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, Integer parameter) {
        this.companyId = parameter;
        try{
            adminChangeCompanyInfoForm.setCompany(companyId);
            adminCompanyTransactionsGrid.setCompany(companyId);
            adminCompanyIPOsGrid.setCompany(companyId);
            adminStartIPODialog.setCompany(companyId);
            adminChangeCompanyInfoForm.updateCompanyData();
            adminCompanyTransactionsGrid.updateList();
            adminCompanyIPOsGrid.updateList();
        } catch (Exception e){
            Notification.show("Wystąpił błąd: " + e.getMessage());
        }
    }
}
