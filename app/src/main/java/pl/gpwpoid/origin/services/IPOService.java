package pl.gpwpoid.origin.services;

import pl.gpwpoid.origin.models.company.IPO;
import pl.gpwpoid.origin.repositories.views.IPOListItem;

import java.util.List;
import java.util.Optional;

public interface IPOService {
    List<IPOListItem> getActiveIPOListItems();

    Optional<IPO> getActiveIPOById(Integer ipoId);
}
