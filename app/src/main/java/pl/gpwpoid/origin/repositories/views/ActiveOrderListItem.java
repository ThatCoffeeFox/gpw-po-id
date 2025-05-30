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
    private Integer walletId;
    private String orderType;
    private Integer shareAmount;
    private BigDecimal sharePrice;
    private Date orderStartDate;
    private Date orderExpirationDate;
}
