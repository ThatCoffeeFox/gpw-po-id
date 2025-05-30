package pl.gpwpoid.origin.factories;

import org.springframework.stereotype.Component;
import pl.gpwpoid.origin.models.wallet.ExternalTransfer;
import pl.gpwpoid.origin.models.wallet.Wallet;

import java.math.BigDecimal;
import java.util.Date;

@Component
public class ExternalTransferFactory {
    public ExternalTransfer createTransfer(
            ExternalTransfer.TransferType transferType,
            BigDecimal amount,
            Wallet wallet,
            Date date,
            String accountNumber) {
        if(transferType == null)
            throw new IllegalArgumentException("transferType cannot be null");
        if(amount.compareTo(BigDecimal.valueOf(0)) <= 0)
            throw new IllegalArgumentException("amount must be greater than 0");
        if(wallet == null)
            throw new IllegalArgumentException("wallet cannot be null");
        if(date == null)
            throw new IllegalArgumentException("date cannot be null");
        if(accountNumber == null)
            throw new IllegalArgumentException("accountNumber cannot be null");

        ExternalTransfer externalTransfer = new ExternalTransfer();
        externalTransfer.setAmount(amount);
        externalTransfer.setAccountNumber(accountNumber);
        externalTransfer.setDate(date);
        externalTransfer.setType(transferType);
        externalTransfer.setWallet(wallet);

        return externalTransfer;
    }
}
