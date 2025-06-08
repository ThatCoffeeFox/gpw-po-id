package pl.gpwpoid.origin.repositories.views;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletListViewItem {
    private Integer walletId;
    private String walletName;
    private BigDecimal walletFunds;
    private Long walletShares;
    private BigDecimal walletSharesValue;
}
