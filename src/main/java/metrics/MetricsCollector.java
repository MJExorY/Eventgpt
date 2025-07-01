package metrics;

import org.simulation.Agent;
import zones.Zone;

import java.util.List;
import java.util.Map;

/**
 * Interface zur Erfassung beliebiger Metriken während der Simulation.
 */
public interface MetricsCollector {
    void registerMetric(String name);

    void recordMetric(String metricName, Object value);

    Map<String, List<Object>> getAllMetrics();

    void recordZoneEntry(Agent agent, Zone zone);

    void recordZoneExit(Agent agent, Zone zone);

    void recordPanicEscape(Agent agent, Zone zone);

    void recordEventTriggered(String eventType);

    void recordTimeInZone(String zoneName, long ticks);

    void recordQueueWait(String zoneName, long ticks);

}
