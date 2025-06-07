package pl.gpwpoid.origin.services.implementations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.gpwpoid.origin.models.company.IPO;
import pl.gpwpoid.origin.models.order.Subscription;
import pl.gpwpoid.origin.repositories.IPORepository;
import pl.gpwpoid.origin.services.CompanyService;
import pl.gpwpoid.origin.services.IPOService;
import pl.gpwpoid.origin.services.ProcessIPOService;
import pl.gpwpoid.origin.services.SubscriptionService;

import java.util.Comparator;
import java.util.List;

@Service
public class ProcessIPOServiceImpl implements ProcessIPOService {
    private final IPOService ipoService;
    private final SubscriptionService subscriptionService;
    private final CompanyService companyService;

    @Autowired
    public ProcessIPOServiceImpl(IPOService ipoService, SubscriptionService subscriptionService, CompanyService companyService) {
        this.ipoService = ipoService;
        this.subscriptionService = subscriptionService;
        this.companyService = companyService;
    }

    @Transactional
    @Override
    public void processFinishedIPOS(){
        List<IPO> iposToProcess = ipoService.findIPOsToProcess();

        for(IPO ipo : iposToProcess)
            processIPO(ipo);
    }

    private void processIPO(IPO ipo){
        Integer sharesSum = subscriptionService.getSharesSumByIPOId(ipo.getIpoId());
        if(sharesSum == null)
            sharesSum = 0;

        Integer ipoSharesAmount = ipo.getSharesAmount();

        List<Subscription> subscriptionsToModify = subscriptionService.getSubscriptionsByIPOId(ipo.getIpoId());

        if(sharesSum <= ipoSharesAmount)
            for(Subscription subscription : subscriptionsToModify)
                subscription.setSharesAssigned(subscription.getSharesAmount());
        else
            setSharesAssignedProportionally(subscriptionsToModify, sharesSum, ipoSharesAmount);

        subscriptionService.saveModifiedSubscriptions(subscriptionsToModify);
        ipo.setProcessed(true);
        companyService.setTradable(ipo.getCompany().getCompanyId(), true);
        ipoService.saveProcessedIPO(ipo);
    }

    private void setSharesAssignedProportionally(List<Subscription> subscriptionsToModify, Integer sharesSum, Integer ipoSharesAmount) {
        Integer sumSharesAssigned = 0;

        for(Subscription subscription : subscriptionsToModify){
            int sharesAssigned = (int) ((double) subscription.getSharesAmount() * ipoSharesAmount / sharesSum);
            subscription.setSharesAssigned(sharesAssigned);
            sumSharesAssigned += sharesAssigned;
        }

        int remainder = ipoSharesAmount - sumSharesAssigned;

        subscriptionsToModify.sort(Comparator.comparing(Subscription::getSharesAmount).reversed());

        for(int i = 0; i < remainder; i++){
            Subscription subscription = subscriptionsToModify.get(i);
            subscription.setSharesAssigned(subscription.getSharesAssigned() + 1);
        }
    }
}
