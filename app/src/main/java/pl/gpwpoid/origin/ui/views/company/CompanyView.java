package pl.gpwpoid.origin.ui.views.company;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.shared.Registration;
import org.springframework.beans.factory.annotation.Autowired;
import pl.gpwpoid.origin.models.company.Company;
import pl.gpwpoid.origin.services.*;
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
    private final ChartUpdateBroadcaster broadcaster;
    private Registration broadcasterRegistration;

    private final CompanyChart companyChart;
    private final OrderForm orderForm;
    private final ActiveOrdersGrid activeOrdersGrid;

    private Integer companyId;
    private Company company;

    @Autowired
    public CompanyView(CompanyService companyService,
                       TransactionService transactionService,
                       OrderService orderService,
                       WalletsService walletsService,
                       ChartUpdateBroadcaster broadcaster) {
        this.companyService = companyService;
        this.transactionService = transactionService;
        this.orderService = orderService;
        this.walletsService = walletsService;
        this.activeOrdersGrid = new ActiveOrdersGrid(orderService);
        this.orderForm = new OrderForm(orderService, walletsService);
        this.companyChart = new CompanyChart(transactionService);
        this.broadcaster = broadcaster;

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
            updateChart();

            if (SecurityUtils.isLoggedIn()) {
                orderForm.setCompanyId(companyId);
                activeOrdersGrid.updateList();
            }
        } catch (Exception e) {
            Notification.show("Nieprawidłowy adres: " + e.getMessage(),
                    4000, Notification.Position.MIDDLE);
        }
    }

    private void updateChart() {
        companyChart.loadAndRenderData(companyId);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        if (companyId != null) {
            broadcasterRegistration = broadcaster.register(companyId, id -> {
                attachEvent.getUI().access(() -> {
                    Notification.show("Aktualizacja danych spółki...", 1000, Notification.Position.BOTTOM_STRETCH);
                    updateChart();
                });
            });
        }
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        if (broadcasterRegistration != null) {
            broadcasterRegistration.remove();
            broadcasterRegistration = null;
        }
        super.onDetach(detachEvent);
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