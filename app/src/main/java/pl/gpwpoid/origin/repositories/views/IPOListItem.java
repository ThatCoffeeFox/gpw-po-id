package pl.gpwpoid.origin.repositories.views;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class IPOListItem {
    private Integer ipoId;
    private Integer companyId;
    private String companyName;

    private Integer sharesAmount;
    private BigDecimal ipoPrice;
    private Date subscriptionStart;
    private Date subscriptionEnd;
}
