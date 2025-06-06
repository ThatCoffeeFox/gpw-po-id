package pl.gpwpoid.origin.services;

import com.vaadin.flow.shared.Registration;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Service
public class ChartUpdateBroadcaster {
    private final Map<Integer, LinkedList<Consumer<Integer>>> listeners = new ConcurrentHashMap<>();

    public synchronized Registration register(Integer companyId, Consumer<Integer> listener) {
        listeners.computeIfAbsent(companyId, k -> new LinkedList<>()).add(listener);

        return () -> {
            synchronized (ChartUpdateBroadcaster.class) {
                if (listeners.containsKey(companyId)) {
                    listeners.get(companyId).remove(listener);
                    if (listeners.get(companyId).isEmpty()) {
                        listeners.remove(companyId);
                    }
                }
            }
        };
    }

    public synchronized void broadcast(Integer companyId) {
        if (listeners.containsKey(companyId)) {
            listeners.get(companyId).removeIf(listener -> {
                try {
                    listener.accept(companyId);
                    return false;
                } catch (Exception e) {
                    return true;
                }
            });
        }
    }

    public Set<Integer> getActiveCompanyIds() {
        return listeners.keySet();
    }
}