package pl.gpwpoid.origin.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.gpwpoid.origin.repositories.TransactionRepository;

import java.time.LocalDateTime;

@Service
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
        broadcaster.broadcastPulse();
        LocalDateTime fiveSecondsAgo = LocalDateTime.now().minusSeconds(5);

        for (Integer companyId : broadcaster.getActiveCompanyIds()) {
            boolean hasTransactions = transactionRepository.existsByBuyOrder_Company_CompanyIdAndDateAfter(companyId, fiveSecondsAgo);

            if (!hasTransactions) {
                log.debug("Wysyłanie pulsu podtrzymującego dla spółki ID: {}. Brak transakcji.", companyId);
                broadcaster.broadcast(companyId);
            }
        }
    }
}