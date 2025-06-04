package pl.gpwpoid.origin.repositories.views;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionListItem {
    private Date subscriptionDate;
    private String walletName;
    private String companyName;
    private Integer sharesAmount;
    private BigDecimal sharePrice;
    private Integer sharesAssigned;
}
