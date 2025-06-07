package pl.gpwpoid.origin.services;

import com.vaadin.flow.shared.Registration;

import java.util.Set;
import java.util.function.Consumer;

public interface ChartUpdateBroadcaster {

    @FunctionalInterface
    interface ChartUpdateListener {
        void onChartUpdate(Integer companyId);
    }

    @FunctionalInterface
    interface GlobalUpdateListener {
        void onUpdate(Integer companyId);
    }

    @FunctionalInterface
    interface PulsarListener {
        void onPulse();
    }

    Registration register(Integer companyId, ChartUpdateListener listener);
    void unregister(Integer companyId, ChartUpdateListener listener);
    void broadcast(Integer companyId);
    void broadcastPulse();
    Set<Integer> getActiveCompanyIds();

    Registration register(GlobalUpdateListener listener);
    Registration register(PulsarListener listener);
    void unregister(GlobalUpdateListener listener);
}