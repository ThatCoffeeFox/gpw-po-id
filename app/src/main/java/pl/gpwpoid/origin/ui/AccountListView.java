package pl.gpwpoid.origin.ui;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import pl.gpwpoid.origin.account.Account;
import pl.gpwpoid.origin.account.AccountInfo;
import pl.gpwpoid.origin.account.AccountRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Route("accounts") 
@PageTitle("Accounts List")



public class AccountListView extends VerticalLayout {

    private final AccountRepository accountRepository; 
    private Grid<Account> grid = new Grid<>(Account.class, false); 

    
    public AccountListView(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;

        setSizeFull(); 
        configureGrid();
        add(grid); 
        updateList(); 
    }

    private void configureGrid() {
        grid.setSizeFull();

        
        
        grid.addColumn(Account::getAccountId).setHeader("ID").setSortable(true);
        grid.addColumn(account -> getLatestInfo(account).map(AccountInfo::getEmail).orElse(null)).setHeader("Email").setSortable(true);
        grid.addColumn(account -> getLatestInfo(account).map(AccountInfo::getPhoneNumber).orElse("-")).setHeader("Phone").setSortable(true);
        grid.addColumn(account -> getLatestInfo(account).map(AccountInfo::getFirstName).orElse("")).setHeader("First Name").setSortable(true);
        grid.addColumn(account -> getLatestInfo(account).map(AccountInfo::getLastName).orElse("")).setHeader("Last Name").setSortable(true);


        grid.getColumns().forEach(col -> col.setAutoWidth(true));
    }

    private void updateList() {
        
        List<Account> accounts = accountRepository.findAll();
        
        grid.setItems(accounts);
    }

    private Optional<AccountInfo> getLatestInfo(Account account) {
        return account.getInfos().stream()
                .max(Comparator.comparing(info -> info.getId().getUpdatedAt()));
    }
}