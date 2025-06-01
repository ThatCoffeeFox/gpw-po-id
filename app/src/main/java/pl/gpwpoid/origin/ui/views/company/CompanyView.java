package pl.gpwpoid.origin.ui.views.company;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import pl.gpwpoid.origin.models.company.Company;
import pl.gpwpoid.origin.services.CompanyService;
import pl.gpwpoid.origin.services.OrderService;
import pl.gpwpoid.origin.services.TransactionService;
import pl.gpwpoid.origin.services.WalletsService;
import pl.gpwpoid.origin.ui.views.MainLayout;
import pl.gpwpoid.origin.utils.SecurityUtils;

import java.util.Optional;

@Route(value = "companies", layout = MainLayout.class)
@AnonymousAllowed
public class CompanyView extends VerticalLayout implements HasUrlParameter<Integer> {
    private final CompanyService companyService;
    private final OrderService orderService;
    private final TransactionService transactionService;
    private final WalletsService walletsService;

    private final CompanyChart companyChart;
    private final OrderForm orderForm;
    private final ActiveOrdersGrid activeOrdersGrid;

    private Integer companyId;
    private Company company;

    @Autowired
    public CompanyView(CompanyService companyService,
                       TransactionService transactionService,
                       OrderService orderService,
                       WalletsService walletsService){
        this.companyService = companyService;
        this.transactionService = transactionService;
        this.orderService = orderService;
        this.walletsService = walletsService;
        this.activeOrdersGrid = new ActiveOrdersGrid(orderService);
        this.orderForm = new OrderForm(orderService, walletsService);
        this.companyChart = new CompanyChart(transactionService);

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        VerticalLayout bottomLayout = new VerticalLayout();
        bottomLayout.setWidthFull();
        bottomLayout.setAlignItems(Alignment.START);

        if (SecurityUtils.isLoggedIn()) {
            bottomLayout.add(orderForm);
            orderForm.setWidth("50%");
            bottomLayout.add(activeOrdersGrid);
        } else {
            bottomLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        }

        add(companyChart, bottomLayout);
        setFlexGrow(1, companyChart);
        setFlexGrow(1, bottomLayout);
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, Integer parameter) {
        this.companyId = parameter;
        try {
            loadCompanyDetails();
            companyChart.loadAndRenderData(companyId);

            if (SecurityUtils.isLoggedIn()) {
                orderForm.setCompanyId(companyId);
                activeOrdersGrid.updateList();
            }
        } catch (Exception e) {
            Notification.show("Nieprawidłowy adres: " + e.getMessage(),
                    4000, Notification.Position.MIDDLE);
        }
    }

    private void loadCompanyDetails() {
        if (companyId != null) {
            Optional<Company> currentCompany = companyService.getCompanyById(companyId);
            if (currentCompany.isPresent()) {
                company = currentCompany.get();
            } else {
                Notification.show("Firma o ID " + companyId + " nie została znaleziona",
                        4000, Notification.Position.MIDDLE);
            }
        } else {
            Notification.show("Brak ID firmy", 4000, Notification.Position.MIDDLE);
        }
    }
}