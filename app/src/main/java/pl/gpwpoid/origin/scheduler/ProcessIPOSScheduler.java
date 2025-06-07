package pl.gpwpoid.origin.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.gpwpoid.origin.services.ProcessIPOService;

@Component
public class ProcessIPOSScheduler {
    private final ProcessIPOService processIPOService;

    @Autowired
    public ProcessIPOSScheduler(ProcessIPOService processIPOService) {
        this.processIPOService = processIPOService;
    }

    @Scheduled(initialDelay = 15000, fixedDelay = 30000)
    public void processFinishedIPOS() {
        processIPOService.processFinishedIPOS();
    }
}
