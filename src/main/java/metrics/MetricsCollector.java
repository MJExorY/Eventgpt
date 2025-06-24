package metrics;

import org.simulation.Agent;
import org.simulation.Zone;

import java.util.List;
import java.util.Map;

/**
 * Interface zur Erfassung beliebiger Metriken während der Simulation.
 */
public interface MetricsCollector {
    void registerMetric(String name);

    void record(String metricName, Object value);

    Map<String, List<Object>> getAllMetrics();

    void recordZoneEntry(Agent agent, Zone zone);

    void recordZoneExit(Agent agent, Zone zone);

    void recordPanicEscape(Agent agent, Zone zone);

    void recordEventTriggered(String eventType);

    void recordTimeInZone(String zoneName, long ticks);

    void recordQueueWait(String zoneName, long ticks);
    
}
