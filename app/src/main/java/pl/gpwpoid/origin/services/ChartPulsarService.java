package pl.gpwpoid.origin.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.gpwpoid.origin.repositories.TransactionRepository;

@Service
@Lazy
public class ChartPulsarService {

    private static final Logger log = LoggerFactory.getLogger(ChartPulsarService.class);
    private final ChartUpdateBroadcaster broadcaster;
    private final TransactionRepository transactionRepository;

    public ChartPulsarService(@Lazy ChartUpdateBroadcaster broadcaster, TransactionRepository transactionRepository) {
        this.broadcaster = broadcaster;
        this.transactionRepository = transactionRepository;
    }

    @Scheduled(fixedRate = 5000)
    public void pulseCharts() {
        log.trace("Uruchamianie pulsara wykresów...");

        for (Integer companyId : broadcaster.getActiveCompanyIds()) {
            log.debug("Wysyłanie pulsu dla spółki ID: {}", companyId);
            broadcaster.broadcast(companyId);
        }

        log.debug("Wysyłanie globalnego pulsu odświeżającego.");
        broadcaster.broadcastGlobal();
    }
}