package pl.gpwpoid.origin.services;

import com.vaadin.flow.shared.Registration;

import java.util.Set;

public interface ChartUpdateBroadcaster {

    Registration register(Integer companyId, ChartUpdateListener listener);

    void unregister(Integer companyId, ChartUpdateListener listener);

    void broadcast(Integer companyId);

    Set<Integer> getActiveCompanyIds();

    Registration register(GlobalUpdateListener listener);

    void unregister(GlobalUpdateListener listener);

    void broadcastGlobal();

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
}