package pl.gpwpoid.origin.ui.views;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import pl.gpwpoid.origin.repositories.views.AccountListItem;
import pl.gpwpoid.origin.services.AccountService;

import java.util.Collection;
import java.util.List;

@Route("accounts") // URL będzie /accounts
@PageTitle("Lista Kont")
@RolesAllowed("admin")
public class AccountsListView extends VerticalLayout {

    private final AccountService accountService;

    private final Grid<AccountListItem> grid = new Grid<>(AccountListItem.class);

    @Autowired
    public AccountsListView(AccountService accountService) {
        this.accountService = accountService;

        setSizeFull();
        setSpacing(true);
        add(new H3("Wszystkie Konta Użytkowników"));
        configureGrid();
        add(grid);
        loadAccountListItems();
    }

    private void configureGrid() {
        grid.setSizeFull();
        grid.removeAllColumns();

        grid.addColumn(AccountListItem::getAccountId).setHeader("ID Konta").setSortable(true).setFlexGrow(0);
        grid.addColumn(AccountListItem::getFirstName).setHeader("Imię").setSortable(true).setFlexGrow(1);
        grid.addColumn(AccountListItem::getSecondaryName).setHeader("Drugie Imię").setSortable(true).setFlexGrow(1);
        grid.addColumn(AccountListItem::getLastName).setHeader("Nazwisko").setSortable(true).setFlexGrow(1);
        grid.addColumn(AccountListItem::getEmail).setHeader("Email").setSortable(true).setFlexGrow(2);
        grid.addColumn(AccountListItem::getPesel).setHeader("PESEL").setSortable(true).setFlexGrow(1);
        grid.addColumn(AccountListItem::getPhoneNumber).setHeader("Numer Telefonu").setSortable(true).setFlexGrow(1);

        grid.addColumn(AccountListItem::getStreet)
                .setHeader("Ulica")
                .setSortable(true).setFlexGrow(1);
        grid.addColumn(AccountListItem::getStreetNumber)
                .setHeader("Nr Budynku")
                .setSortable(true).setFlexGrow(0);
        grid.addColumn(AccountListItem::getApartmentNumber)
                .setHeader("Nr Mieszkania")
                .setSortable(true).setFlexGrow(0);
        grid.addColumn(AccountListItem::getPostalCodeValue)
                .setHeader("Kod Pocztowy")
                .setSortable(true).setFlexGrow(1);
        grid.addColumn(AccountListItem::getTownName)
                .setHeader("Miasto")
                .setSortable(true).setFlexGrow(1);


//        grid.asSingleSelect().addValueChangeListener(event -> {
//            AccountListItem selectedItem = event.getValue();
//            if (selectedItem != null) {
//                // Przekierowujemy do AccountDetailView używając accountId z DTO
//                // Upewnij się, że AccountDetailView akceptuje parametr typu Long lub Integer,
//                // w zależności od tego, jaki typ ma accountId w AccountListItem i Account.
//                // Jeśli accountId w AccountListItem jest Long, a w AccountDetailView Integer, dokonaj konwersji.
//                UI.getCurrent().navigate(AccountDetailView.class, selectedItem.getAccountId().intValue()); // Przykład konwersji na int
//            }
//        });
    }

    private void loadAccountListItems() {
        Collection<AccountListItem> accountItems = accountService.getAccountViewList();
        grid.setItems(accountItems);
    }
}