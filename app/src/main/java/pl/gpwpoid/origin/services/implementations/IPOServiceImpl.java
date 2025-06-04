package pl.gpwpoid.origin.services.implementations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.gpwpoid.origin.models.company.IPO;
import pl.gpwpoid.origin.repositories.IPORepository;
import pl.gpwpoid.origin.repositories.views.IPOListItem;
import pl.gpwpoid.origin.services.IPOService;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class IPOServiceImpl implements IPOService {
    private final IPORepository ipoRepository;

    @Autowired
    IPOServiceImpl(IPORepository ipoRepository){
        this.ipoRepository = ipoRepository;
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
}
