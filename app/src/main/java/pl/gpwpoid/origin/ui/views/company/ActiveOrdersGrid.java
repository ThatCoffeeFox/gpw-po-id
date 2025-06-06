package pl.gpwpoid.origin.ui.views.company;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.NumberRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import pl.gpwpoid.origin.repositories.views.ActiveOrderListItem;
import pl.gpwpoid.origin.services.OrderService;

import java.text.NumberFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class ActiveOrdersGrid extends VerticalLayout {

    private final Grid<ActiveOrderListItem> grid = new Grid<>();
    private final OrderService orderService;

    @Autowired
    public ActiveOrdersGrid(OrderService orderService) {
        this.orderService = orderService;
        configureGrid();
        add(new H3("Aktywne zlecenia"), grid);
        setSizeFull();
    }

    private void configureGrid() {
        grid.addColumn(ActiveOrderListItem::getWalletName)
                .setHeader("Portfel")
                .setSortable(true)
                .setAutoWidth(true);

        grid.addColumn(item -> translateOrderType(item.getOrderType()))
                .setHeader("Typ zlecenia")
                .setSortable(true)
                .setAutoWidth(true);

        grid.addColumn(ActiveOrderListItem::getSharesAmount)
                .setHeader("Ilość akcji")
                .setSortable(true)
                .setAutoWidth(true);

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pl", "PL"));
        grid.addColumn(new NumberRenderer<>(
                        ActiveOrderListItem::getSharePrice,
                        currencyFormat))
                .setHeader("Cena za akcję")
                .setSortable(true)
                .setAutoWidth(true);

        DateTimeFormatter dateFormatter = DateTimeFormatter
                .ofPattern("dd.MM.yyyy")
                .withZone(ZoneId.systemDefault());

        grid.addColumn(ActiveOrderListItem::getOrderStartDate)
                .setHeader("Data złożenia")
                .setSortable(true)
                .setAutoWidth(true);

        grid.addColumn(ActiveOrderListItem::getOrderExpirationDate)
                .setHeader("Data wygaśnięcia")
                .setSortable(true)
                .setAutoWidth(true);

        grid.addComponentColumn(item -> {
                    Button cancelButton = new Button("Anuluj", event -> {
                        try {
                            orderService.cancelOrder(item.getOrderId());
                            Notification.show("Zlecenie zostało anulowane", 3000, Notification.Position.MIDDLE);
                            updateList(); // Odśwież listę po anulowaniu
                        } catch (Exception e) {
                            Notification.show("Błąd podczas anulowania zlecenia: " + e.getMessage(),
                                    5000, Notification.Position.MIDDLE);
                        }
                    });
                    cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
                    return cancelButton;
                }).setHeader("Akcje")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.setSizeFull();
        grid.setAllRowsVisible(true);
    }

    private String translateOrderType(String orderType) {
        if ("buy".equalsIgnoreCase(orderType)) return "Kupno";
        if ("sell".equalsIgnoreCase(orderType)) return "Sprzedaż";
        return orderType;
    }

    public void updateList() {
        grid.setItems(orderService.getActiveOrderListItemsForLoggedInAccount());
    }
}