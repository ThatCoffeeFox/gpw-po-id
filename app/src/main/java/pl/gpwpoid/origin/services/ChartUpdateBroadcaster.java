package pl.gpwpoid.origin.services;

import com.vaadin.flow.shared.Registration;
import pl.gpwpoid.origin.repositories.DTO.TransactionDTO;

import java.util.Set;

public interface ChartUpdateBroadcaster {

    Registration register(Integer companyId, ChartUpdateListener listener);

    void unregister(Integer companyId, ChartUpdateListener listener);

    void broadcast(TransactionDTO transactionDTO);

    Set<Integer> getActiveCompanyIds();

    Registration register(GlobalUpdateListener listener);

    void unregister(GlobalUpdateListener listener);

    void broadcastGlobal();

    void broadcastPulse(Integer companyId);

    @FunctionalInterface
    interface ChartUpdateListener {
        void onChartUpdate(TransactionDTO transactionData);
    }

    @FunctionalInterface
    interface GlobalUpdateListener {
        void onUpdate(Integer companyId);
    }
}