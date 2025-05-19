package pl.gpwpoid.origin.ui.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import pl.gpwpoid.origin.utils.SecurityUtils;

@AnonymousAllowed
@Route("/")
public class MainLayout extends AppLayout {

    public MainLayout() {

        H1 title = new H1("Giełda Papierów Wartościowych");
        title.addClassName("logo");

        HorizontalLayout header;
        if(SecurityUtils.isLoggedIn()) {
            H2 name = new H2(String.valueOf(SecurityUtils.getAuthenticatedAccountId()));
            Button logout = new Button("Logout", click -> SecurityUtils.logout());
            header = new HorizontalLayout(title, name, logout);
        } else {
            Button login = new Button("Login", click -> UI.getCurrent().navigate("/login"));
            header = new HorizontalLayout(title, login);
        }

        addToNavbar(header);
    }
}
