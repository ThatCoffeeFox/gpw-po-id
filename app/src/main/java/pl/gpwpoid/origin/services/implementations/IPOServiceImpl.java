package pl.gpwpoid.origin.services.implementations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.gpwpoid.origin.factories.IPOFactory;
import pl.gpwpoid.origin.models.company.Company;
import pl.gpwpoid.origin.models.company.IPO;
import pl.gpwpoid.origin.models.wallet.Wallet;
import pl.gpwpoid.origin.repositories.IPORepository;
import pl.gpwpoid.origin.repositories.views.AdminIPOListItem;
import pl.gpwpoid.origin.repositories.views.IPOListItem;
import pl.gpwpoid.origin.services.CompanyService;
import pl.gpwpoid.origin.services.IPOService;
import pl.gpwpoid.origin.services.WalletsService;
import pl.gpwpoid.origin.ui.views.DTO.IPODTO;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class IPOServiceImpl implements IPOService {
    private final IPORepository ipoRepository;
    private final CompanyService companyService;
    private final WalletsService walletsService;
    private final IPOFactory ipoFactory;

    @Autowired
    IPOServiceImpl(IPORepository ipoRepository, CompanyService companyService, WalletsService walletsService, IPOFactory ipoFactory) {
        this.ipoRepository = ipoRepository;
        this.companyService = companyService;
        this.walletsService = walletsService;
        this.ipoFactory = ipoFactory;
    }

    @Override
    public List<IPOListItem> getActiveIPOListItems() {
        return ipoRepository.findActiveIPOListItems();
    }

    @Override
    public Optional<IPO> getActiveIPOById(Integer ipoId) {
        Optional<IPO> ipo = ipoRepository.findById(Long.valueOf(ipoId));
        if(ipo.isPresent() &&
                (new Date()).before(ipo.get().getSubscriptionEnd()) &&
                (new Date()).after(ipo.get().getSubscriptionStart())){
            return ipo;
        }
        return Optional.empty();

    }

    @Override
    public Collection<AdminIPOListItem> getAdminIPOListItemsByCompanyId(Integer companyId) {
        return ipoRepository.getAdminIPOListItemsByCompanyId(companyId);
    }

    @Override
    @Transactional
    public void addIPO(IPODTO ipoDTO) {
        Company company = companyService.getCompanyById(ipoDTO.getCompanyId()).get();
        Wallet wallet = walletsService.getWalletById(ipoDTO.getWalletOwnerId()).get();
        IPO ipo = ipoFactory.createIPO(
                ipoDTO.getSharePrice(),
                ipoDTO.getSharesAmount(),
                ipoDTO.getSubscriptionEnd(),
                company,
                wallet
        );

        ipoRepository.save(ipo);
    }

    @Override
    public Boolean hasActiveIPO(Integer companyId) {
        return ipoRepository.hasActiveIPO(companyId) != null;
    }

    @Override
    public List<IPO> findIPOsToProcess() {
        return ipoRepository.findIPOsToProcess();
    }

    @Override
    public void saveProcessedIPO(IPO ipo) {
        ipoRepository.save(ipo);
    }
}
