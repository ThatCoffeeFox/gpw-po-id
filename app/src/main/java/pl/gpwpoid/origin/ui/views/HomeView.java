package pl.gpwpoid.origin.ui.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.shared.Registration;
import org.springframework.beans.factory.annotation.Autowired;
import pl.gpwpoid.origin.repositories.views.CompanyListItem;
import pl.gpwpoid.origin.services.ChartUpdateBroadcaster;
import pl.gpwpoid.origin.services.CompanyService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Strona Główna - SGPW")
@AnonymousAllowed
public class HomeView extends VerticalLayout {

    private final Grid<CompanyListItem> topCompaniesGrid = new Grid<>();
    private final CompanyService companyService;
    private Registration broadcasterRegistration;
    private final ChartUpdateBroadcaster broadcaster;
    private ListDataProvider<CompanyListItem> dataProvider;

    @Autowired
    public HomeView(CompanyService companyService, ChartUpdateBroadcaster broadcaster) {
        this.companyService = companyService;
        this.broadcaster = broadcaster;


        H1 title = new H1("Witaj w Symulacji Giełdy Papierów Wartościowych");
        Paragraph description = new Paragraph(
                "SGPW to platforma demonstracyjna, która pozwala na śledzenie notowań fikcyjnych spółek, " +
                        "składanie zleceń kupna i sprzedaży oraz zarządzanie wirtualnym portfelem. " +
                        "Sprawdź poniżej listę najdroższych firm notowanych na naszej giełdzie!"
        );
        description.getStyle().set("max-width", "800px").set("text-align", "center");
        H2 topCompaniesHeader = new H2("Top 5 Najdroższych Spółek");

        configureGrid();
        loadTopCompanies();

        setAlignItems(Alignment.CENTER);
        topCompaniesGrid.getStyle().set("margin-left", "auto").set("margin-right", "auto");
        getStyle().set("padding", "var(--lumo-space-l)");

        add(title, description, topCompaniesHeader, topCompaniesGrid);
    }

    private void configureGrid() {
        topCompaniesGrid.setWidth("100%");
        topCompaniesGrid.setMaxWidth("1000px");

        topCompaniesGrid.addColumn(CompanyListItem::getName).setHeader("Nazwa spółki");
        topCompaniesGrid.addColumn(CompanyListItem::getCode).setHeader("Kod");
        topCompaniesGrid.addColumn(item -> formatPrice(item.getCurrentSharePrice()))
                .setHeader("Cena za akcję")
                .setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.END);
        topCompaniesGrid.addComponentColumn(this::createChangeSpan)
                .setHeader("Zmiana (24h)")
                .setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.END);


        topCompaniesGrid.setAllRowsVisible(true);


        topCompaniesGrid.addItemClickListener(event -> {
            UI.getCurrent().navigate("companies/" + event.getItem().getCompanyId());
        });
    }

    private void loadTopCompanies() {
        List<CompanyListItem> companyList = companyService.getTop5MostValuableCompanies();
        this.dataProvider = new ListDataProvider<>(companyList);

        topCompaniesGrid.setDataProvider(dataProvider);
    }

    private String formatPrice(BigDecimal price) {
        if (price == null) return "Brak danych";
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

    private void refreshGridItems() {
        if (dataProvider == null || dataProvider.getItems().isEmpty()) {
            return;
        }

        Map<Integer, BigDecimal> freshMap = companyService.getCompaniesViewList().stream()
                .collect(Collectors.toMap(CompanyListItem::getCompanyId, CompanyListItem::getCurrentSharePrice));

        dataProvider.getItems().forEach(currentItem -> {
            BigDecimal newPrice = freshMap.get(currentItem.getCompanyId());
            if (newPrice == null) return;

            if (!Objects.equals(currentItem.getCurrentSharePrice(), newPrice)) {
                currentItem.setCurrentSharePrice(newPrice);
                dataProvider.refreshItem(currentItem);
            }
        });
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        UI ui = attachEvent.getUI();

        broadcasterRegistration = broadcaster.register(id -> {
            ui.access(this::refreshGridItems);
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