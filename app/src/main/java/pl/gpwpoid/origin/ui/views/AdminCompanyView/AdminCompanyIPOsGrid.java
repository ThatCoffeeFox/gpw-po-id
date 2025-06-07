package pl.gpwpoid.origin.ui.views.AdminCompanyView;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import pl.gpwpoid.origin.models.company.IPO;
import pl.gpwpoid.origin.repositories.views.AdminIPOListItem;
import pl.gpwpoid.origin.repositories.views.IPOListItem;
import pl.gpwpoid.origin.repositories.views.TransactionListItem;
import pl.gpwpoid.origin.services.CompanyService;
import pl.gpwpoid.origin.services.IPOService;
import pl.gpwpoid.origin.services.TransactionService;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Locale;

public class AdminCompanyIPOsGrid extends VerticalLayout {
    private final IPOService ipoService;
    private Integer companyId;

    private final Grid<AdminIPOListItem> grid = new Grid<>();

    private static final DecimalFormat FUNDS_FORMATTER = new DecimalFormat(
            "#,##0.00",
            DecimalFormatSymbols.getInstance(new Locale("pl", "PL"))
    );

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public AdminCompanyIPOsGrid(IPOService ipoService) {
        this.ipoService = ipoService;

        add(new H3("Historia IPO"), grid);
        configureGrid();
    }

    private void configureGrid() {
        grid.addColumn(AdminIPOListItem::getWalletOwner)
                .setHeader("Właściciel portfela")
                .setAutoWidth(true);

        grid.addColumn(AdminIPOListItem::getSharesAmount)
                .setHeader("Ilość akcji")
                .setAutoWidth(true);

        grid.addColumn(this::formatFunds)
                .setHeader("Cena akcji")
                .setAutoWidth(true);

        grid.addColumn(this::formatStartDate)
                .setHeader("Data rozpoczęcia")
                .setAutoWidth(true);

        grid.addColumn(this::formatEndDate)
                .setHeader("Data zakończenia")
                .setAutoWidth(true);

        grid.setPageSize(10);
    }

    private String formatFunds(AdminIPOListItem item) {
        if(item.getIpoPrice() == null)
            return "";
        return FUNDS_FORMATTER.format(item.getIpoPrice()) + " zł";
    }

    private String formatStartDate(AdminIPOListItem item) {
        if(item.getSubsctiptionStart() == null)
            return "";
        LocalDateTime date = item.getSubsctiptionStart().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        return date.format(DATE_FORMATTER);
    }

    private String formatEndDate(AdminIPOListItem item) {
        if(item.getSubsctiptionEnd() == null)
            return "";
        LocalDateTime date = item.getSubsctiptionEnd().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        return date.format(DATE_FORMATTER);
    }

    public void setCompany(Integer companyId) {
        this.companyId = companyId;
    }

    public void updateList(){
        Collection<AdminIPOListItem> items = ipoService.getAdminIPOListItemsByCompanyId(companyId);
        grid.setItems(items);
    }
}
