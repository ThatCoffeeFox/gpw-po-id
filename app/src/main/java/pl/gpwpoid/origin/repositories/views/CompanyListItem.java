package pl.gpwpoid.origin.repositories.views;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(of = "companyId")
public class CompanyListItem {
    private Integer companyId;
    private String name;
    private String code;
    private String townName;
    private String postalCode;
    private String streetName;
    private String streetNumber;
    private String apartmentNumber;
    private BigDecimal currentSharePrice;
    private BigDecimal lastDaySharePrice;
}
