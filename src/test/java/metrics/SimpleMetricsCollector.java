package metrics;

import org.simulation.Agent;
import zones.Zone;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Eine einfache, nicht-funktionale Implementierung des MetricsCollector Interface.
 * Wird ausschließlich für Tests verwendet, bei denen keine echte Metrikaufzeichnung notwendig ist.
 * <p>
 * Alle Methoden sind sogenannte "No-ops" (no operation) und haben keine Wirkung.
 *
 * @author Dorothea Ziegler
 */
public class SimpleMetricsCollector implements MetricsCollector {
    @Override
    public void registerMetric(String name) {
        // No-op
    }

    @Override
    public void recordMetric(String metricName, Object value) {
        // No-op
    }

    @Override
    public Map<String, List<Object>> getAllMetrics() {
        return Collections.emptyMap();
    }

    @Override
    public void recordZoneEntry(Agent agent, Zone zone) {
        // No-op
    }

    @Override
    public void recordZoneExit(Agent agent, Zone zone) {
        // No-op
    }

    @Override
    public void recordPanicEscape(Agent agent, Zone zone) {
        // No-op
    }

    @Override
    public void recordEventTriggered(String eventType) {
        // No-op
    }

    @Override
    public void recordTimeInZone(String zoneName, long ticks) {
        // No-op
    }

    @Override
    public void recordQueueWait(String zoneName, long ticks) {
        // No-op
    }
}
