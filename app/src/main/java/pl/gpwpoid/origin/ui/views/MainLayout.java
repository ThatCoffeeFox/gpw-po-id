package pl.gpwpoid.origin.ui.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vaadin.hilla.auth.EndpointAccessChecker;
import org.springframework.beans.factory.annotation.Autowired;
import pl.gpwpoid.origin.models.account.AccountInfo;
import pl.gpwpoid.origin.repositories.views.AccountListItem;
import pl.gpwpoid.origin.services.AccountService;
import pl.gpwpoid.origin.utils.SecurityUtils;

@AnonymousAllowed
@Route("/")
public class MainLayout extends AppLayout {

    private final AccessAnnotationChecker accessChecker;
    private final AccountService accountService;

    @Autowired
    public MainLayout(AccountService accountService, AccessAnnotationChecker accessChecker) {
        this.accountService = accountService;
        this.accessChecker = accessChecker;

        setPrimarySection(Section.NAVBAR);
        addHeaderContent();
        addDrawerContent();
    }

    private void addHeaderContent() {
        H1 title = new H1("SGPW");

        title.addClassNames(LumoUtility.FontSize.XLARGE, LumoUtility.Margin.NONE);
        RouterLink logoLink = new RouterLink();
        logoLink.setRoute(MainLayout.class);

        logoLink.add(title);
        logoLink.addClassName("logo-link");

        HorizontalLayout header = new HorizontalLayout(logoLink);

        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(logoLink);
        header.setWidthFull();
        header.addClassNames(LumoUtility.Padding.Vertical.NONE, LumoUtility.Padding.Horizontal.MEDIUM);

        SecurityUtils.getAuthenticatedUser().ifPresentOrElse(
                user -> {
                    Button logoutButton = new Button("Wyloguj się", e -> SecurityUtils.logout());

                    AccountInfo userInfo = accountService.getNewestAccountInfoItemById(user.getAccountId());
                    Avatar avatar = new Avatar(userInfo.getFirstName() + " " + userInfo.getLastName());

                    header.add(avatar, logoutButton);
                },
                () -> {
                    Button login = new Button("Zaloguj się", e -> UI.getCurrent().navigate(LoginView.class));
                    Button register = new Button("Rejestracja",  e -> UI.getCurrent().navigate(RegistrationView.class));

                    header.add(login, register);
                }
        );
        addToNavbar(true, header);
    }

    private void addDrawerContent() {
        SideNav nav =  new SideNav();

        nav.setWidth(null);

        if(accessChecker.hasAccess(AccountsListView.class)) {
            nav.addItem(new SideNavItem("Konta Użytkowników", AccountsListView.class, VaadinIcon.USER_CARD.create()));
        }

        if(accessChecker.hasAccess(CompaniesListView.class)) {
            nav.addItem(new SideNavItem("Firmy", CompaniesListView.class, VaadinIcon.BUILDING.create()));
        }

        if(accessChecker.hasAccess(WalletsListView.class)) {
            nav.addItem(new SideNavItem("Twoje Portfele", WalletsListView.class, VaadinIcon.WALLET.create()));
        }

        if(accessChecker.hasAccess(ProfileView.class)) {
            nav.addItem(new SideNavItem("Mój Profil", ProfileView.class, VaadinIcon.USER.create()));
        }

        addToDrawer(nav);
    }
}
