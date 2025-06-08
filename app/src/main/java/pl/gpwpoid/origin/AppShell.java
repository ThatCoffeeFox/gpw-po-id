package pl.gpwpoid.origin;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

@Push
@Theme(value = "main", variant = Lumo.LIGHT)
public class AppShell implements AppShellConfigurator {

}