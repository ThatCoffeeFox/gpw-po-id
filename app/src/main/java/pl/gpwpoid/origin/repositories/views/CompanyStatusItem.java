package pl.gpwpoid.origin.repositories.views;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompanyStatusItem {
    private BigDecimal currentPrice;
    private BigDecimal previousPrice;
    private Boolean tradable;
}
