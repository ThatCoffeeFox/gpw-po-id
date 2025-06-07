package pl.gpwpoid.origin.services.implementations;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import pl.gpwpoid.origin.factories.SubscriptionFactory;
import pl.gpwpoid.origin.models.company.IPO;
import pl.gpwpoid.origin.models.order.Subscription;
import pl.gpwpoid.origin.models.wallet.Wallet;
import pl.gpwpoid.origin.repositories.SubscriptionRepository;
import pl.gpwpoid.origin.repositories.views.SubscriptionListItem;
import pl.gpwpoid.origin.services.IPOService;
import pl.gpwpoid.origin.services.SubscriptionService;
import pl.gpwpoid.origin.services.WalletsService;
import pl.gpwpoid.origin.ui.views.DTO.SubscriptionDTO;
import pl.gpwpoid.origin.utils.SecurityUtils;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {
    private final SubscriptionFactory subscriptionFactory;

    private final SubscriptionRepository subscriptionRepository;
    private final WalletsService walletsService;

    private final IPOService ipoService;

    public SubscriptionServiceImpl(SubscriptionFactory subscriptionFactory, SubscriptionRepository subscriptionRepository, WalletsService walletsService, IPOService ipoService) {
        this.subscriptionFactory = subscriptionFactory;

        this.subscriptionRepository = subscriptionRepository;

        this.walletsService = walletsService;
        this.ipoService = ipoService;
    }

    @Override
    public List<SubscriptionListItem> getSubscriptionListItemsForLoggedInAccount() {
        Integer accountId = SecurityUtils.getAuthenticatedAccountId();
        if(accountId == null){
            throw new RuntimeException("there is no logged in user");
        }
        return subscriptionRepository.findSubscriptionListItemsByAccountId(accountId);
    }

    @Override
    public void addSubscription(SubscriptionDTO subscriptionDTO) throws AccessDeniedException {
        Optional<IPO> ipo = ipoService.getActiveIPOById(subscriptionDTO.getIpoId());
        if(ipo.isEmpty()){
            throw new  EntityNotFoundException("There is no active IPO with this ID");
        }

        Optional<Wallet> wallet = walletsService.getWalletById(subscriptionDTO.getWalletId());
        if(wallet.isEmpty()) throw new EntityNotFoundException("This wallet does not exist");
        if (!wallet.get().getAccount().getAccountId().equals(SecurityUtils.getAuthenticatedAccountId())){
            throw new AccessDeniedException("You are not an owner of the wallet");
        }

        if(ipo.get().getIpoPrice().multiply(BigDecimal.valueOf(subscriptionDTO.getSharesAmount()))
                .compareTo(walletsService.getWalletUnblockedFundsById(subscriptionDTO.getWalletId())) > 0){
            throw new RuntimeException("You don't have enough shares/funds");
        }

        try {
            Subscription subscription = subscriptionFactory.createSubscription(subscriptionDTO.getSharesAmount(),wallet.get(),ipo.get());
            subscriptionRepository.save(subscription);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create subscription", e);
        }
    }
}
