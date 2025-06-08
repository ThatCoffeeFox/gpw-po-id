package pl.gpwpoid.origin.services;

import pl.gpwpoid.origin.models.company.IPO;
import pl.gpwpoid.origin.repositories.views.AdminIPOListItem;
import pl.gpwpoid.origin.repositories.views.IPOListItem;
import pl.gpwpoid.origin.ui.views.DTO.IPODTO;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface IPOService {
    List<IPOListItem> getActiveIPOListItems();

    Optional<IPO> getActiveIPOById(Integer ipoId);

    Collection<AdminIPOListItem> getAdminIPOListItemsByCompanyId(Integer companyId);

    void addIPO(IPODTO ipoDTO);

    Boolean hasActiveIPO(Integer companyId);

    List<IPO> findIPOsToProcess();

    void saveProcessedIPO(IPO ipo);

    Optional<BigDecimal> getIpoPriceByCompanyId(Integer companyId);
}
