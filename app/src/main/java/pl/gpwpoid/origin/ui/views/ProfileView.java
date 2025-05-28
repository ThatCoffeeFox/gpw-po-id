package pl.gpwpoid.origin.ui.views;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route("/profile")
@PageTitle("Twój profil")
@PermitAll
public class ProfileView extends VerticalLayout {
}
