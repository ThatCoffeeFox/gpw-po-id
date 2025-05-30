package pl.gpwpoid.origin.ui.views.company;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.data.renderer.NumberRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import pl.gpwpoid.origin.repositories.views.ActiveOrderListItem;
import pl.gpwpoid.origin.services.OrderService;

import java.text.NumberFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
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
        updateList();
    }

    private void configureGrid() {
        grid.addColumn(ActiveOrderListItem::getWalletId)
                .setHeader("Portfel")
                .setSortable(true);

        grid.addColumn(item -> translateOrderType(item.getOrderType()))
                .setHeader("Typ zlecenia")
                .setSortable(true);

        grid.addColumn(ActiveOrderListItem::getShareAmount)
                .setHeader("Ilość akcji")
                .setSortable(true);

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pl", "PL"));
        grid.addColumn(new NumberRenderer<>(
                        ActiveOrderListItem::getSharePrice,
                        currencyFormat))
                .setHeader("Cena za akcję")
                .setSortable(true);

        DateTimeFormatter dateFormatter = DateTimeFormatter
                .ofPattern("dd.MM.yyyy")
                .withZone(ZoneId.systemDefault());

        grid.addColumn(new LocalDateRenderer<>(
                        item -> convertToLocalDate(item.getOrderStartDate()),
                        dateFormatter))
                .setHeader("Data złożenia")
                .setSortable(true);

        grid.addColumn(new LocalDateRenderer<>(
                        item -> convertToLocalDate(item.getOrderExpirationDate()),
                        dateFormatter))
                .setHeader("Data wygaśnięcia")
                .setSortable(true);

        grid.setSizeFull();
        grid.setHeightByRows(true);
        grid.setMaxHeight("500px");
    }

    private LocalDate convertToLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
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