package metrics;

import org.simulation.Agent;
import org.simulation.Zone;

import java.util.*;

/**
 * Standard-Implementierung zur Sammlung von Metriken.
 */
public class DefaultMetricsCollector implements MetricsCollector {
    private final Map<String, List<Object>> data = new HashMap<>();

    @Override
    public void registerMetric(String name) {
        data.putIfAbsent(name, new ArrayList<>());
    }

    @Override
    public void record(String metricName, Object value) {
        // Falls die Metrik noch nicht existiert, legen wir sie jetzt an:
        List<Object> list = data.computeIfAbsent(metricName, k -> new ArrayList<>());
        list.add(value);
    }

    @Override
    public Map<String, List<Object>> getAllMetrics() {
        return data;
    }

    @Override
    public void recordZoneEntry(Agent agent, Zone zone) {
        record("ZoneEntry_" + zone.getType(), agent.hashCode());
    }

    @Override
    public void recordZoneExit(Agent agent, Zone zone) {
        record("ZoneExit_" + zone.getType(), agent.hashCode());
    }

    @Override
    public void recordPanicEscape(Agent agent, Zone zone) {
        int ticks = agent.getPanicTicks();
        double minutes = ticks / 60.0;
        record("PanicDuration", minutes);
    }

    @Override
    public void recordEventTriggered(String eventType) {
        record("EventTriggered_" + eventType, 1);
    }

    @Override
    public void recordTimeInZone(String zoneName, long ticks) {
        record("TimeInZone_" + zoneName, ticks);
    }

    @Override
    public void recordQueueWait(String zoneName, long ticks) {
        record("QueueWait_" + zoneName, ticks);
    }

    public void printZoneEntrySummary() {
        System.out.println("📊 Zonen-Betretungen:");

        int total = 0;

        for (Map.Entry<String, List<Object>> entry : data.entrySet()) {
            if (entry.getKey().startsWith("ZoneEntry_")) {
                String zoneName = entry.getKey().replace("ZoneEntry_", "");
                int count = entry.getValue().size();
                total += count;
                System.out.println("Zone: " + zoneName + " → " + count + " Agenten");
            }
        }

        System.out.println("➤ Insgesamt: " + total + " Besuche");
    }

}