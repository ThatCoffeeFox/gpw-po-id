package pl.gpwpoid.origin.ui.views.adminCompanyListView;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import pl.gpwpoid.origin.repositories.views.AdminCompanyListItem;
import pl.gpwpoid.origin.services.CompanyService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class AdminCompaniesGrid extends VerticalLayout {
    private final CompanyService companyService;
    private final Grid<AdminCompanyListItem> grid = new Grid<>();

    private static final DecimalFormat FUNDS_FORMATTER = new DecimalFormat(
            "#,##0.00",
            DecimalFormatSymbols.getInstance(new Locale("pl", "PL"))
    );

    AdminCompaniesGrid(CompanyService companyService) {
        this.companyService = companyService;
        configureGrid();
        add(new H3("Lista Firm"), grid);
        setSizeFull();
    }

    private void configureGrid() {
        grid.addColumn(new ComponentRenderer<>(this::navigateToCompanyButton))
                .setHeader("Nazwa Firmy")
                .setAutoWidth(true);

        grid.addColumn(AdminCompanyListItem::getCompanyCode)
                .setHeader("Kod Firmy")
                .setSortable(true)
                .setAutoWidth(true);

        grid.addColumn(this::formatFunds)
                .setHeader("Cena akcji")
                .setSortable(true)
                .setAutoWidth(true);

        grid.addColumn(this::calculatePercentage)
                .setHeader("Zmiana w %")
                .setSortable(true)
                .setAutoWidth(true);

        grid.addColumn(new ComponentRenderer<>(this::setTradableButton))
                .setHeader("Tradable")
                .setAutoWidth(true);
    }

    public void updateList() {
        grid.setItems(companyService.getAdminCompanyListItems());
    }

    private String formatFunds(AdminCompanyListItem item) {
        if(item.getCurrentPrice() == null)
            return "";
        return FUNDS_FORMATTER.format(item.getCurrentPrice()) + " zł";
    }

    private String calculatePercentage(AdminCompanyListItem item) {
        if(item.getPreviousPrice() == null)
            return "0.00%";
        BigDecimal percentage = item.getCurrentPrice().divide(item.getPreviousPrice(), 10, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP).add(new BigDecimal("-100"));
        return percentage + "%";
    }

    private Button navigateToCompanyButton(AdminCompanyListItem item){
        Button navigationButton = new Button(item.getCompanyName());
        navigationButton.addClickListener(e -> {
            String url = "/admin/companies/" + item.getCompanyId();
            UI.getCurrent().navigate(url);
        });
        return navigationButton;
    }

    private Button setTradableButton(AdminCompanyListItem item) {
        Button tradableButton = new Button(translateTradable(item));
        tradableButton.addClickListener(e -> {
            String newButtonText;
            Boolean oldTradable = item.getTradable();
            try {
                if(oldTradable) {
                    item.setTradable(false);
                    newButtonText = "Nie";
                }
                else {
                    item.setTradable(true);
                    newButtonText = "Tak";
                }
                tradableButton.setEnabled(false);
                companyService.setTradable(item.getCompanyId(), item.getTradable());
                tradableButton.setText(newButtonText);
            } catch (Exception ex) {
                Notification.show("Wystąpił błąd: " + ex.getMessage(), 5000, Notification.Position.TOP_CENTER);
                item.setTradable(oldTradable);
            } finally {
                tradableButton.setEnabled(true);
            }
        });
        return tradableButton;
    }

    private String translateTradable(AdminCompanyListItem item) {
        if(item.getTradable())
            return "Tak";
        return "Nie";
    }
}
