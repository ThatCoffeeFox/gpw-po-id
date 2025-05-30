package pl.gpwpoid.origin.ui.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import pl.gpwpoid.origin.repositories.views.CompanyListItem;
import pl.gpwpoid.origin.services.CompanyService;

import java.util.Collection;


@Route(value = "companies", layout =  MainLayout.class)
@PageTitle("Companies list")
@AnonymousAllowed
public class CompaniesListView extends VerticalLayout {
    private final CompanyService companyService;
    private final Grid<CompanyListItem> grid = new Grid<>();

    public CompaniesListView(CompanyService companyService) {
        this.companyService = companyService;

        setSizeFull();

        configureGrid();
        add(grid);
        loadCompanyListItems();
    }

    private void configureGrid() {
        grid.setSizeFull();

        grid.addColumn(CompanyListItem::getName).setHeader("Company name").setSortable(true);
        grid.addColumn(CompanyListItem::getCode).setHeader("Code").setSortable(true);
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);

        grid.addItemClickListener(event -> {
            CompanyListItem item = event.getItem();
            if(item != null) {
                UI.getCurrent().navigate("companies/" + item.getCompanyId());
            }
        });

        grid.getColumns().forEach(column -> column.setAutoWidth(true));
    }

    private void loadCompanyListItems() {
        Collection<CompanyListItem> companyList = companyService.getCompaniesViewList();
        grid.setItems(companyList);
    }
}

