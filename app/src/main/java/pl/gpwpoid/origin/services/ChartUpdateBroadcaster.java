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

    Registration register(Integer companyId, ChartUpdateListener listener);
    void unregister(Integer companyId, ChartUpdateListener listener);
    void broadcast(Integer companyId);
    Set<Integer> getActiveCompanyIds();

    Registration register(GlobalUpdateListener listener);
    void unregister(GlobalUpdateListener listener);
}