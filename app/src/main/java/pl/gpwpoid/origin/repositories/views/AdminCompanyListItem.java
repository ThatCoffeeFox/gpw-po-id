package pl.gpwpoid.origin.repositories.views;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AdminCompanyListItem {
    private Integer companyId;
    private String companyName;
    private String companyCode;
    private BigDecimal currentPrice;
    private BigDecimal previousPrice;
    private Boolean tradable;
}
