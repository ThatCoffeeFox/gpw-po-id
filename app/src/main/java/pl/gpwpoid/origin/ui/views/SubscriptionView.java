package pl.gpwpoid.origin.ui.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import pl.gpwpoid.origin.repositories.views.IPOListItem;
import pl.gpwpoid.origin.repositories.views.SubscriptionListItem;
import pl.gpwpoid.origin.repositories.views.WalletListItem;
import pl.gpwpoid.origin.services.IPOService;
import pl.gpwpoid.origin.services.SubscriptionService;
import pl.gpwpoid.origin.services.WalletsService;
import pl.gpwpoid.origin.ui.views.DTO.SubscriptionDTO;
import pl.gpwpoid.origin.utils.SecurityUtils;

import java.text.NumberFormat;
import java.util.Collection;
import java.util.Locale;
import java.util.Objects;

@Route(value = "subscriptions", layout = MainLayout.class)
@PageTitle("Zapisy")
@RolesAllowed({"user", "admin"})
public class SubscriptionView extends VerticalLayout {
    private final ComboBox<IPOListItem> companyComboBox = new ComboBox<>("Wybierz firmę");
    private final ComboBox<WalletListItem> walletComboBox = new ComboBox<>("Wybierz portfel");
    private final IntegerField sharesAmountIntegerField = new IntegerField("Liczba akcji");
    private final Button submitButton = new Button("Zapisz się");
    private final Binder<SubscriptionDTO> binder = new Binder<>(SubscriptionDTO.class);
    private final Grid<IPOListItem> activeIPOGrid = new Grid<>();
    private final Grid<SubscriptionListItem> subscriptionsGrid = new Grid<>();
    private final IPOService ipoService;
    private final SubscriptionService subscriptionService;
    private final WalletsService walletsService;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pl", "PL"));
    private FormLayout subscriptionFormLayout = new FormLayout(
            companyComboBox,
            walletComboBox,
            sharesAmountIntegerField,
            submitButton
    );
    private Collection<WalletListItem> avaibleWallets;
    private Collection<IPOListItem> avaibleCompanies;


    @Autowired
    public SubscriptionView(IPOService ipoService, SubscriptionService subscriptionService, WalletsService walletsService) {
        this.ipoService = ipoService;
        this.subscriptionService = subscriptionService;
        this.walletsService = walletsService;
        configureSubscriptionForm();
        configureActiveIPOGrid();
        configureSubscriptionGrid();
        add(
                new H3("Aktywne IPO"),
                activeIPOGrid,
                new H3("Zapisz się na akcje"),
                subscriptionFormLayout,
                new H3("Twoje zapisy"),
                subscriptionsGrid);
        loadSubscriptionFormData();
        loadActiveIPOGrid();
        loadSubscriptionGrid();
    }

    void loadSubscriptionFormData() {
        Integer accountId = SecurityUtils.getAuthenticatedAccountId();
        this.avaibleWallets = walletsService.getWalletListViewByAccountId(accountId);
        this.avaibleCompanies = ipoService.getActiveIPOListItems();
        walletComboBox.setItems(avaibleWallets);
        companyComboBox.setItems(avaibleCompanies);
    }

    void configureSubscriptionForm() {

        walletComboBox.setItemLabelGenerator(WalletListItem::getName);
        companyComboBox.setItemLabelGenerator(IPOListItem::getCompanyName);
        sharesAmountIntegerField.setMin(0);

        binder.forField(walletComboBox)
                .asRequired("Portfel jest wymagany")
                .withValidator(Objects::nonNull, "Wybierz portfel")
                .bind(dto -> null,
                        (dto, wallet) ->
                                dto.setWalletId(wallet.getWalletId())
                );
        binder.forField(companyComboBox)
                .asRequired("Firma jest wymagana")
                .withValidator(Objects::nonNull, "Wybierz firme")
                .bind(dto -> null,
                        (dto, company) ->
                                dto.setIpoId(company.getIpoId())
                );
        binder.forField(sharesAmountIntegerField)
                .asRequired("Ilos akcji jest wymagana")
                .bind(SubscriptionDTO::getSharesAmount,
                        SubscriptionDTO::setSharesAmount);

        submitButton.addClickListener(e -> {
            SubscriptionDTO dto = new SubscriptionDTO();
            if (binder.writeBeanIfValid(dto)) {
                try {
                    subscriptionService.addSubscription(dto);
                    loadSubscriptionGrid();
                    binder.readBean(new SubscriptionDTO());
                    walletComboBox.clear();
                    companyComboBox.clear();
                    sharesAmountIntegerField.clear();
                    Notification.show("Zapisano na akcje", 4000, Notification.Position.TOP_CENTER);
                } catch (Exception ex) {
                    Notification.show("Wystąpił błąd " + ex.getMessage(), 4000, Notification.Position.TOP_CENTER);
                }
            } else {
                Notification.show("Niepoprawne dane", 4000, Notification.Position.TOP_CENTER);
            }
        });

        subscriptionFormLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );
    }

    private void loadSubscriptionGrid() {
        subscriptionsGrid.setItems(subscriptionService.getSubscriptionListItemsForLoggedInAccount());
    }

    private void configureSubscriptionGrid() {
        subscriptionsGrid.setSizeFull();
        subscriptionsGrid.setAllRowsVisible(true);
        subscriptionsGrid.addColumn(SubscriptionListItem::getWalletName).setHeader("Portfel");
        subscriptionsGrid.addColumn(SubscriptionListItem::getCompanyName).setHeader("Frima");
        subscriptionsGrid.addColumn(SubscriptionListItem::getSharesAmount).setHeader("Zapisane Akcje");
        subscriptionsGrid.addColumn(new NumberRenderer<>(SubscriptionListItem::getSharePrice, currencyFormat)).setHeader("Cena");
        subscriptionsGrid.addColumn(SubscriptionListItem::getSharesAssigned).setHeader("Przyznane Akcje");
        activeIPOGrid.getColumns().forEach(column -> column.setAutoWidth(true));
    }

    private void loadActiveIPOGrid() {
        activeIPOGrid.setItems(ipoService.getActiveIPOListItems());
    }

    private void configureActiveIPOGrid() {
        activeIPOGrid.setSizeFull();
        activeIPOGrid.setAllRowsVisible(true);
        activeIPOGrid.addColumn(IPOListItem::getCompanyName).setHeader("Firma");
        activeIPOGrid.addColumn(new NumberRenderer<>(IPOListItem::getIpoPrice, currencyFormat)).setHeader("Cena");
        activeIPOGrid.addColumn(IPOListItem::getSharesAmount).setHeader("Liczba Akcji");
        activeIPOGrid.addColumn(IPOListItem::getSubscriptionStart).setHeader("Data rozpoczęcia");
        activeIPOGrid.addColumn(IPOListItem::getSubscriptionEnd).setHeader("Data zakończenia");
        activeIPOGrid.getColumns().forEach(column -> column.setAutoWidth(true));
    }

}
