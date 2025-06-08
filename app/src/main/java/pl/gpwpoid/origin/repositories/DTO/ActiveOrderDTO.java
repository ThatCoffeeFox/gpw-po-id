package pl.gpwpoid.origin.repositories.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ActiveOrderDTO {
    private Integer orderId;
    private String orderType;
    private Integer sharesAmount;
    private BigDecimal sharePrice;
    private Date orderStartDate;
    private Date orderExpirationDate;
}
