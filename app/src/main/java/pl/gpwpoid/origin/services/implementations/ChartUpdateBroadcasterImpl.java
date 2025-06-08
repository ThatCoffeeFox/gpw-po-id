package pl.gpwpoid.origin.services.implementations;

import com.vaadin.flow.shared.Registration;
import org.springframework.stereotype.Component;
import pl.gpwpoid.origin.services.ChartUpdateBroadcaster;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Component
public class ChartUpdateBroadcasterImpl implements ChartUpdateBroadcaster {

    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Map<Integer, List<ChartUpdateListener>> listenersByCompany = new HashMap<>();
    private final List<GlobalUpdateListener> globalListeners = new LinkedList<>();

    @Override
    public synchronized Registration register(Integer companyId, ChartUpdateListener listener) {
        listenersByCompany.computeIfAbsent(companyId, k -> new LinkedList<>()).add(listener);
        return () -> unregister(companyId, listener);
    }


    @Override
    public synchronized void unregister(Integer companyId, ChartUpdateListener listener) {
        if (listenersByCompany.containsKey(companyId)) {
            listenersByCompany.get(companyId).remove(listener);
            if (listenersByCompany.get(companyId).isEmpty()) {
                listenersByCompany.remove(companyId);
            }
        }
    }

    @Override
    public synchronized Registration register(GlobalUpdateListener listener) {
        globalListeners.add(listener);
        return () -> unregister(listener);
    }

    @Override
    public synchronized void unregister(GlobalUpdateListener listener) {
        globalListeners.remove(listener);
    }

    @Override
    public void broadcast(Integer companyId) {
        List<ChartUpdateListener> companyListeners;
        synchronized (this) {
            companyListeners = listenersByCompany.get(companyId);
        }
        if (companyListeners != null) {
            for (ChartUpdateListener listener : companyListeners) {
                executor.execute(() -> listener.onChartUpdate(companyId));
            }
        }

        synchronized (this) {
            for (GlobalUpdateListener listener : globalListeners) {
                executor.execute(() -> listener.onUpdate(companyId));
            }
        }
    }

    @Override
    public synchronized Set<Integer> getActiveCompanyIds() {
        return new HashSet<>(listenersByCompany.keySet());
    }

    @Override
    public void broadcastGlobal() {
        synchronized (this) {
            for (GlobalUpdateListener listener : globalListeners) {
                executor.execute(() -> listener.onUpdate(null));
            }
        }
    }
}