package pl.gpwpoid.origin.repositories.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class WalletCompanyDTO {
    private Integer walletId;
    private BigDecimal founds;
    private BigDecimal unlocked_founds;

    private Integer companyId;
    private Integer shareAmount;
    private Integer unblockedShareAmount;
    private BigDecimal sharePrice;
}
