package pl.gpwpoid.origin.repositories.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TransactionDTO {
    private Date date;
    private Integer sharesAmount;
    private BigDecimal sharePrice;
}