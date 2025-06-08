package pl.gpwpoid.origin.ui.views.company;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.shared.Registration;
import org.springframework.beans.factory.annotation.Autowired;
import pl.gpwpoid.origin.repositories.views.CompanyListItem;
import pl.gpwpoid.origin.services.*;
import pl.gpwpoid.origin.ui.views.MainLayout;
import pl.gpwpoid.origin.utils.SecurityUtils;

import java.math.BigDecimal;

@Route(value = "companies", layout = MainLayout.class)
@AnonymousAllowed
public class CompanyView extends VerticalLayout implements HasUrlParameter<Integer> {
    private final CompanyService companyService;
    private final OrderService orderService;
    private final TransactionService transactionService;
    private final WalletsService walletsService;
    private final IPOService ipoService;
    private final ChartUpdateBroadcaster broadcaster;
    private final ChartPulsarService chartPulsarService;
    private final CompanyInfoTablet companyInfoTablet;
    private final CompanyChart companyChart;
    private final OrderForm orderForm;
    private final ActiveOrdersGrid activeOrdersGrid;
    private final CompanyUserTransactionsGrid transactionsGrid;
    private Registration broadcasterRegistration;
    private Integer companyId;
    private CompanyListItem companyInfo;
    private CompanyListItem companyInfoCache;
    private BigDecimal currentPriceCache;

    @Autowired
    public CompanyView(CompanyService companyService,
                       TransactionService transactionService,
                       OrderService orderService,
                       WalletsService walletsService,
                       ChartUpdateBroadcaster broadcaster,
                       ChartPulsarService chartPulsarService,
                       IPOService ipoService) {
        this.companyService = companyService;
        this.transactionService = transactionService;
        this.orderService = orderService;
        this.walletsService = walletsService;
        this.ipoService = ipoService;
        this.activeOrdersGrid = new ActiveOrdersGrid(orderService);
        this.orderForm = new OrderForm(orderService, walletsService);
        this.broadcaster = broadcaster;
        this.chartPulsarService = chartPulsarService;
        this.companyChart = new CompanyChart(transactionService, ipoService, broadcaster);
        this.transactionsGrid = new CompanyUserTransactionsGrid(transactionService, walletsService);
        this.companyInfoTablet = new CompanyInfoTablet();

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        VerticalLayout bottomLayout = new VerticalLayout();
        bottomLayout.setWidthFull();
        bottomLayout.setAlignItems(Alignment.CENTER);
        bottomLayout.setJustifyContentMode(JustifyContentMode.CENTER);

        if (SecurityUtils.isLoggedIn()) {
            HorizontalLayout temp = new HorizontalLayout();
            temp.setWidthFull();
            temp.setAlignItems(Alignment.CENTER);
            temp.setJustifyContentMode(JustifyContentMode.CENTER);
            temp.add(orderForm, companyInfoTablet);
            bottomLayout.add(temp, transactionsGrid);
            orderForm.setWidth("50%");
            bottomLayout.add(activeOrdersGrid);
        } else {
            bottomLayout.add(companyInfoTablet);
        }
        add(companyChart, bottomLayout);
        setFlexGrow(1, companyChart);
        setFlexGrow(1, bottomLayout);
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, Integer parameter) {
        if (!parameter.equals(this.companyId)) {
            this.companyInfoCache = null;
            this.currentPriceCache = null;
        }

        this.companyId = parameter;

        if (this.companyInfoCache == null) {
            loadCompanyDetailsAndFullUpdate();
        } else {
            updateCurrentPriceAndRefreshTablet();
        }

        try {
            loadCompanyDetails();
            updateChart();

            if (SecurityUtils.isLoggedIn()) {
                orderForm.setCompanyId(companyId);
                transactionsGrid.setCompanyId(companyId);
                activeOrdersGrid.setCompanyId(companyId);
                activeOrdersGrid.updateList();
            }
        } catch (Exception e) {
            Notification.show("Nieprawidłowy adres: " + e.getMessage(),
                    4000, Notification.Position.MIDDLE);
        }
    }

    private void updateCurrentPriceAndRefreshTablet() {
        if (companyId == null) return;
        BigDecimal newPrice = transactionService.getShareValueByCompanyId(companyId);

        this.currentPriceCache = newPrice;

        updateCompanyInfoTablet();
    }

    private void loadCompanyDetailsAndFullUpdate() {
        if (companyId == null) return;
        try {
            this.companyInfoCache = companyService.getCompanyItemById(companyId);
            if (this.companyInfoCache == null) {
                Notification.show("Firma o ID " + companyId + " nie została znaleziona.", 4000, Notification.Position.MIDDLE);
                return;
            }
            this.currentPriceCache = this.companyInfoCache.getCurrentSharePrice();

            updateCompanyInfoTablet();

        } catch (Exception e) {
            Notification.show("Nieprawidłowy adres: " + e.getMessage(), 4000, Notification.Position.MIDDLE);
        }
    }

    private void updateCompanyInfoTablet() {
        this.companyInfo = companyService.getCompanyItemById(companyId);
        if (companyInfo != null) {
            companyInfoTablet.updateInfo(
                    companyInfo.getName(),
                    companyInfo.getCode(),
                    companyInfo.getTownName(),
                    companyInfo.getPostalCode(),
                    companyInfo.getStreetName(),
                    companyInfo.getStreetNumber(),
                    companyInfo.getApartmentNumber(),
                    this.currentPriceCache,
                    companyInfoCache.getLastDaySharePrice());
        }
    }

    private void updateChart() {
        companyChart.loadAndRenderData(companyId);
    }

    private void updateTransactionsGrid() {
        transactionsGrid.refresh();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        if (companyId != null) {
            // Zmieniamy logikę wewnątrz słuchacza
            broadcasterRegistration = broadcaster.register(companyId, id -> {
                attachEvent.getUI().access(() -> {
                    updateCurrentPriceAndRefreshTablet();

                    if (SecurityUtils.isLoggedIn()) {
                        updateTransactionsGrid();
                        activeOrdersGrid.updateList();
                    }
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
            this.companyInfo = companyService.getCompanyItemById(companyId);
            if (this.companyInfo == null) {
                Notification.show("Firma o ID " + companyId + " nie została znaleziona",
                        4000, Notification.Position.MIDDLE);
            }
        } else {
            Notification.show("Brak ID firmy", 4000, Notification.Position.MIDDLE);
        }
    }
}