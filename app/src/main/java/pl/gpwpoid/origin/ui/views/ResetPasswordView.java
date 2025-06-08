package pl.gpwpoid.origin.ui.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import pl.gpwpoid.origin.services.AccountService;

@Route("reset-password")
@AnonymousAllowed
public class ResetPasswordView extends VerticalLayout {

    private final AccountService accountService;

    @Autowired
    public ResetPasswordView(AccountService accountService) {
        this.accountService = accountService;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        H2 title = new H2("Resetuj hasło");

        EmailField emailField = new EmailField("Twój adres email");
        emailField.setRequiredIndicatorVisible(true);
        emailField.setClearButtonVisible(true);

        PasswordField newPasswordField = new PasswordField("Nowe hasło");
        newPasswordField.setRequiredIndicatorVisible(true);

        PasswordField confirmPasswordField = new PasswordField("Potwierdź nowe hasło");
        confirmPasswordField.setRequiredIndicatorVisible(true);

        Button resetButton = new Button("Zmień hasło", event -> resetPassword(
                emailField.getValue(),
                newPasswordField.getValue(),
                confirmPasswordField.getValue()
        ));
        resetButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        add(title, emailField, newPasswordField, confirmPasswordField, resetButton);
    }

    private void resetPassword(String email, String newPassword, String confirmPassword) {

        if (email.isBlank() || newPassword.isBlank()) {
            Notification.show("Wszystkie pola muszą być wypełnione.", 3000, Notification.Position.MIDDLE);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Notification.show("Wprowadzone hasła nie są identyczne.", 3000, Notification.Position.MIDDLE);
            return;
        }

        try {

            accountService.updatePassword(email, newPassword);


            Notification.show("Hasło zostało pomyślnie zmienione. Możesz się teraz zalogować.", 4000, Notification.Position.MIDDLE);

            UI.getCurrent().navigate(LoginView.class);

        } catch (IllegalArgumentException | IllegalStateException e) {

            Notification.show(e.getMessage(), 4000, Notification.Position.MIDDLE);
        }
    }
}