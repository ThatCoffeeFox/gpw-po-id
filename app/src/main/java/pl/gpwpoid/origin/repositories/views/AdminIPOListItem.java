package pl.gpwpoid.origin.repositories.views;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminIPOListItem {
    private String walletOwner;
    private Integer sharesAmount;
    private BigDecimal ipoPrice;
    private Date subsctiptionStart;
    private Date subsctiptionEnd;
}
