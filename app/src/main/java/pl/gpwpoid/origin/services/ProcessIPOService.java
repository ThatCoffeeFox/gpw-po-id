package pl.gpwpoid.origin.services;

import pl.gpwpoid.origin.models.company.IPO;
import pl.gpwpoid.origin.models.order.Subscription;

import java.util.List;

public interface ProcessIPOService {
    void processFinishedIPOS();
}
