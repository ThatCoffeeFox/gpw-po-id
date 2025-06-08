package pl.gpwpoid.origin.ui.views.company;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CompanyInfoTablet extends VerticalLayout {


    private final H2 companyName = new H2();
    private final Span companyCode = new Span();


    private final Span fullAddress = new Span();


    private final Span priceValue = new Span();
    private final Span priceCurrency = new Span("PLN");


    private final Span priceChange = new Span();

    public CompanyInfoTablet() {

        companyName.getStyle().set("margin", "0").set("font-size", "var(--lumo-font-size-xxl)").set("line-height", "var(--lumo-line-height-m)");
        companyCode.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-size", "var(--lumo-font-size-l)").set("margin-left", "var(--lumo-space-s)");


        fullAddress.getStyle().set("font-size", "var(--lumo-font-size-s)").set("color", "var(--lumo-secondary-text-color)");


        String valueStyle = "var(--lumo-font-size-xl)";
        priceValue.getStyle().set("font-size", valueStyle).set("font-weight", "600");
        priceChange.getStyle().set("font-size", valueStyle).set("font-weight", "600");
        priceCurrency.getStyle().set("font-size", "var(--lumo-font-size-m)").set("color", "var(--lumo-secondary-text-color)").set("margin-left", "var(--lumo-space-xs)");


        HorizontalLayout headerLayout = new HorizontalLayout(companyName, companyCode);
        headerLayout.setAlignItems(Alignment.BASELINE);


        Icon addressIcon = VaadinIcon.MAP_MARKER.create();
        addressIcon.getStyle().set("color", "var(--lumo-tertiary-text-color)").set("width", "var(--lumo-font-size-s)").set("height", "var(--lumo-font-size-s)");
        HorizontalLayout addressLayout = new HorizontalLayout(addressIcon, fullAddress);
        addressLayout.setAlignItems(Alignment.CENTER);
        addressLayout.setSpacing(true);


        HorizontalLayout priceContainer = new HorizontalLayout(priceValue, priceCurrency);
        priceContainer.setAlignItems(Alignment.BASELINE);
        priceContainer.setSpacing(false);

        VerticalLayout priceBlock = createInfoBlock("Aktualna cena", priceContainer);
        VerticalLayout changeBlock = createInfoBlock("Zmiana (24h)", priceChange);


        HorizontalLayout detailsLayout = new HorizontalLayout(priceBlock, changeBlock);
        detailsLayout.setWidth("100%");
        detailsLayout.setJustifyContentMode(JustifyContentMode.START);
        detailsLayout.setSpacing(true);

        detailsLayout.expand(priceBlock, changeBlock);


        setSpacing(true);
        setWidth("auto");
        getStyle().set("border", "1px solid var(--lumo-contrast-10pct)").set("border-radius", "var(--lumo-border-radius-l)").set("padding", "var(--lumo-space-l)");


        add(headerLayout, addressLayout, detailsLayout);
    }

    private VerticalLayout createInfoBlock(String labelText, Component valueComponent) {
        Span label = new Span(labelText);
        label.getStyle().set("font-size", "var(--lumo-font-size-s)").set("color", "var(--lumo-secondary-text-color)");

        VerticalLayout block = new VerticalLayout(label, valueComponent);
        block.setSpacing(false);
        block.setPadding(false);
        block.setAlignItems(Alignment.START);
        return block;
    }


    public void updateInfo(String name, String code,
                           String townName, String postalCode, String streetName, String streetNumber, String apartmentNumber,
                           BigDecimal newPrice, BigDecimal lastPrice) {
        companyName.setText(name);
        companyCode.setText(code);


        String streetPart = Stream.of("ul. " + streetName, streetNumber)
                .filter(StringUtils::hasText)
                .collect(Collectors.joining(" "));
        if (StringUtils.hasText(apartmentNumber)) {
            streetPart += "/" + apartmentNumber;
        }
        String townPart = Stream.of(postalCode, townName)
                .filter(StringUtils::hasText)
                .collect(Collectors.joining(" "));

        String finalAddress = Stream.of(streetPart, townPart)
                .filter(StringUtils::hasText)
                .collect(Collectors.joining(", "));

        fullAddress.setText(StringUtils.hasText(finalAddress) ? finalAddress : "Brak danych adresowych");

        fullAddress.getParent().ifPresent(layout -> layout.setVisible(StringUtils.hasText(finalAddress)));


        priceValue.setText(Optional.ofNullable(newPrice).map(p -> String.format("%.2f", p)).orElse("---"));
        priceCurrency.setVisible(newPrice != null);

        if (lastPrice != null && newPrice != null && lastPrice.compareTo(BigDecimal.ZERO) != 0) {
            BigDecimal change = newPrice.subtract(lastPrice);
            BigDecimal percentageChange = change.divide(lastPrice, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
            String formattedChange = String.format("%s%.2f%%", percentageChange.signum() >= 0 ? "+" : "", percentageChange);
            priceChange.setText(formattedChange);
            priceChange.getStyle().remove("color");
            if (percentageChange.signum() > 0) priceChange.getStyle().set("color", "var(--lumo-success-text-color)");
            else if (percentageChange.signum() < 0) priceChange.getStyle().set("color", "var(--lumo-error-text-color)");
            else priceChange.getStyle().set("color", "var(--lumo-secondary-text-color)");
        } else {
            priceChange.setText("---");
            priceChange.getStyle().remove("color");
        }
    }


    public void reset() {
        companyName.setText("");
        companyCode.setText("");
        fullAddress.setText("---");
        fullAddress.getParent().ifPresent(layout -> layout.setVisible(false));
        priceValue.setText("---");
        priceCurrency.setVisible(false);
        priceChange.setText("---");
        priceChange.getStyle().remove("color");
    }
}