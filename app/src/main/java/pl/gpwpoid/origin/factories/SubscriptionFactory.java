package pl.gpwpoid.origin.factories;

import org.springframework.stereotype.Component;
import pl.gpwpoid.origin.models.company.IPO;
import pl.gpwpoid.origin.models.order.Subscription;
import pl.gpwpoid.origin.models.wallet.Wallet;
import pl.gpwpoid.origin.ui.views.DTO.SubscriptionDTO;

import java.util.Date;

@Component
public class SubscriptionFactory {

    public Subscription createSubscription(Integer sharesAmount, Wallet wallet, IPO ipo){
        Subscription subscription = new Subscription();

        subscription.setSharesAmount(sharesAmount);
        subscription.setWallet(wallet);
        subscription.setIpo(ipo);

        subscription.setDate(new Date());
        subscription.setSharesAssigned(null);

        return subscription;
    }
}
