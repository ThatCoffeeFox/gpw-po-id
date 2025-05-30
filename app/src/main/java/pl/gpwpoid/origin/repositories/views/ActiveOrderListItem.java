package pl.gpwpoid.origin.repositories.views;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ActiveOrderListItem {
    private Integer orderId;
    private String walletName;
    private String orderType;
    private Integer sharesAmount;
    private BigDecimal sharePrice;
    private Date orderStartDate;
    private Date orderExpirationDate;
}
