package pl.gpwpoid.origin.services;

import pl.gpwpoid.origin.repositories.views.SubscriptionListItem;
import pl.gpwpoid.origin.ui.views.DTO.SubscriptionDTO;

import java.nio.file.AccessDeniedException;
import java.util.List;

public interface SubscriptionService {
    List<SubscriptionListItem> getSubscriptionListItemsForLoggedInAccount();
    void addSubscription(SubscriptionDTO subscriptionDTO) throws AccessDeniedException;
}
