package pl.gpwpoid.origin.utils;

import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import javax.swing.text.html.Option;
import java.util.Optional;

public final class SecurityUtils {
    private SecurityUtils() {}

    public static Optional<UserDetails> getAuthenticatedUser() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        if (securityContext == null) {

            return Optional.empty();
        }

        Authentication authentication = securityContext.getAuthentication();
        if(authentication == null) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            return Optional.of((UserDetails) principal);
        }

        return Optional.empty();
    }

    public static String getAuthenticatedEmail() {
        if(getAuthenticatedUser().isEmpty()) {
            return null;
        }
        return getAuthenticatedUser().get().getUsername();
    }

    public static boolean isLoggedIn() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return
                authentication != null
                && authentication.isAuthenticated()
                && !(authentication.getPrincipal() instanceof String
                        && authentication.getPrincipal().equals("anonymousUser"));
    }

    public static void logout() {
        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.logout(
                VaadinServletRequest.getCurrent().getHttpServletRequest(),
                null,
                null
        );

        Optional.ofNullable(VaadinSession.getCurrent()).ifPresent(VaadinSession::close);
    }
}
