package pl.gpwpoid.origin.repositories.views;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionWalletListItem {
    private String orderType;
    private Date date;
    private BigDecimal amount;
    private Integer sharesAmount;
    private String companyCode;
    private Integer companyId;
    private Integer walletId;
}
