package pl.gpwpoid.origin.ui.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.shared.Registration;
import pl.gpwpoid.origin.repositories.views.CompanyListItem;
import pl.gpwpoid.origin.services.ChartUpdateBroadcaster;
import pl.gpwpoid.origin.services.CompanyService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


@Route(value = "/companies", layout = MainLayout.class)
@PageTitle("Spółki Giełdowe")
@AnonymousAllowed
public class CompaniesListView extends VerticalLayout {
    private final CompanyService companyService;
    private final ChartUpdateBroadcaster broadcaster;
    private Registration broadcasterRegistration;

    private final Grid<CompanyListItem> grid = new Grid<>();

    private ListDataProvider<CompanyListItem> dataProvider;
    private Map<Integer, CompanyListItem> companyMap;

    public CompaniesListView(CompanyService companyService, ChartUpdateBroadcaster broadcaster) {
        this.companyService = companyService;
        this.broadcaster = broadcaster;

        setSizeFull();
        configureGrid();
        add(grid);
        loadCompanyListItems();
    }

    private void configureGrid() {
        grid.setSizeFull();

        grid.addColumn(CompanyListItem::getName).setHeader("Nazwa spółki").setSortable(true);
        grid.addColumn(CompanyListItem::getCode).setHeader("Kod").setSortable(true);

        grid.addColumn(item -> formatPrice(item.getCurrentSharePrice()))
                .setHeader("Cena za akcję")
                .setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.END)
                .setSortable(true);

        grid.addComponentColumn(this::createChangeSpan)
                .setHeader("Zmiana (24h)")
                .setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.END)
                .setSortable(true);

        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.addItemClickListener(event -> {
            CompanyListItem item = event.getItem();
            if (item != null) {
                UI.getCurrent().navigate("companies/" + item.getCompanyId());
            }
        });

        grid.getColumns().forEach(column -> column.setAutoWidth(true));
    }

    private String formatPrice(BigDecimal price) {
        if (price == null) {
            return "Brak danych";
        }
        return String.format("%.2f PLN", price);
    }

    private Span createChangeSpan(CompanyListItem item) {
        Span changeSpan = new Span();
        BigDecimal currentPrice = item.getCurrentSharePrice();
        BigDecimal lastDayPrice = item.getLastDaySharePrice();

        if (currentPrice == null || lastDayPrice == null || lastDayPrice.compareTo(BigDecimal.ZERO) == 0) {
            changeSpan.setText("---");
            return changeSpan;
        }

        BigDecimal change = currentPrice.subtract(lastDayPrice);
        BigDecimal percentageChange = change.divide(lastDayPrice, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));

        String formattedChange = String.format("%s%.2f%%", percentageChange.signum() >= 0 ? "+" : "", percentageChange);
        changeSpan.setText(formattedChange);

        if (percentageChange.signum() > 0) {
            changeSpan.getStyle().set("color", "var(--lumo-success-text-color)");
        } else if (percentageChange.signum() < 0) {
            changeSpan.getStyle().set("color", "var(--lumo-error-text-color)");
        }

        return changeSpan;
    }

    private void loadCompanyListItems() {
        List<CompanyListItem> companyList = companyService.getCompaniesViewList();
        this.companyMap = new ConcurrentHashMap<>(
                companyList.stream().collect(Collectors.toMap(CompanyListItem::getCompanyId, item -> item))
        );
        this.dataProvider = new ListDataProvider<>(companyList);
        grid.setDataProvider(dataProvider);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        UI ui = attachEvent.getUI();

        broadcasterRegistration = broadcaster.register(companyId -> {
            ui.access(() -> {
                if (companyMap.containsKey(companyId)) {
                    CompanyListItem updatedItemData = companyService.getCompanyItemById(companyId);
                    if (updatedItemData != null) {
                        CompanyListItem itemToRefresh = companyMap.get(companyId);
                        itemToRefresh.setCurrentSharePrice(updatedItemData.getCurrentSharePrice());
                        dataProvider.refreshItem(itemToRefresh);
                    }
                }
            });
        });
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        if (broadcasterRegistration != null) {
            broadcasterRegistration.remove();
            broadcasterRegistration = null;
        }
        super.onDetach(detachEvent);
    }
}