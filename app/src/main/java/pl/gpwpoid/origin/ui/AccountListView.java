package pl.gpwpoid.origin.ui;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll; 
import pl.gpwpoid.origin.account.Account;
import pl.gpwpoid.origin.account.AccountRepository;

import java.util.List;

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
        grid.addColumn(Account::getEmail).setHeader("Email").setSortable(true);
        grid.addColumn(Account::getFirstName).setHeader("First Name").setSortable(true);
        grid.addColumn(Account::getLastName).setHeader("Last Name").setSortable(true);
        grid.addColumn(Account::getPhoneNumber).setHeader("Phone Number");
        grid.addColumn(Account::getCreatedAt).setHeader("Created At").setSortable(true);
        
        
        

        

        
        grid.getColumns().forEach(col -> col.setAutoWidth(true));
    }

    private void updateList() {
        
        List<Account> accounts = accountRepository.findAll();
        
        grid.setItems(accounts);
    }
}