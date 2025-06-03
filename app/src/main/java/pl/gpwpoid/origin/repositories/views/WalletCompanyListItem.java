package pl.gpwpoid.origin.repositories.views;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class WalletCompanyListItem {
    String companyName;
    String companyCode;
    BigDecimal currentSharePrice;
    BigDecimal previousSharePrice;
    Integer sharesAmount;
    Integer companyId;
}
