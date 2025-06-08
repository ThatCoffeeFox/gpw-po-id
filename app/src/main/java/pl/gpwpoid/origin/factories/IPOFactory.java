package pl.gpwpoid.origin.factories;

import org.springframework.stereotype.Component;
import pl.gpwpoid.origin.models.company.Company;
import pl.gpwpoid.origin.models.company.IPO;
import pl.gpwpoid.origin.models.wallet.Wallet;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashSet;

@Component
public class IPOFactory {

    public IPO createIPO(
            BigDecimal sharePrice,
            Integer sharesAmount,
            LocalDateTime subscriptionEnd,
            Company company,
            Wallet wallet
    ) {
        if (sharePrice == null)
            throw new IllegalArgumentException("sharePrice cannot be null");
        if (sharesAmount == null)
            throw new IllegalArgumentException("sharesAmount cannot be null");
        if (subscriptionEnd == null)
            throw new IllegalArgumentException("subscriptionEnd cannot be null");
        if (company == null)
            throw new IllegalArgumentException("company cannot be null");
        if (wallet == null)
            throw new IllegalArgumentException("wallet cannot be null");
        ZoneId zoneId = ZoneId.of("UTC");
        Date dateSubscriptionEnd = Date.from(subscriptionEnd.atZone(zoneId).toInstant());
        if (dateSubscriptionEnd.before(new Date()))
            throw new IllegalArgumentException("subscriptionEnd cannot be in the past");

        IPO ipo = new IPO();
        ipo.setSubscriptions(new HashSet<>());
        ipo.setCompany(company);
        ipo.setPaymentWallet(wallet);
        ipo.setSubscriptionStart(new Date());
        ipo.setSubscriptionEnd(dateSubscriptionEnd);
        ipo.setSharesAmount(sharesAmount);
        ipo.setIpoPrice(sharePrice);
        ipo.setProcessed(false);

        company.getIpos().add(ipo);

        return ipo;
    }
}
