package metrics;

import org.simulation.Agent;
import zones.Zone;

import java.util.*;

/**
 * Implementiert das MetricsCollector-Interface zur Erfassung von Simulationsmetriken.
 * Speichert Ereignisse wie Zonen-Betretungen, Wartezeiten, Panikfluchten etc.
 * Die gesammelten Daten können z. B. zur Auswertung oder Visualisierung verwendet werden.
 *
 * @author cb-235866
 */
public class DefaultMetricsCollector implements MetricsCollector {
    private final Map<String, List<Object>> data = new HashMap<>();

    @Override
    public void registerMetric(String name) {
        data.putIfAbsent(name, new ArrayList<>());
    }

    @Override
    public void recordMetric(String metricName, Object value) {
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
        recordMetric("ZoneEntry_" + zone.getType(), agent.hashCode());
    }

    @Override
    public void recordZoneExit(Agent agent, Zone zone) {
        recordMetric("ZoneExit_" + zone.getType(), agent.hashCode());
    }

    @Override
    public void recordPanicEscape(Agent agent, Zone zone) {
        int ticks = agent.getPanicTicks();
        double minutes = ticks / 60.0;
        recordMetric("PanicDuration", minutes);
    }

    @Override
    public void recordEventTriggered(String eventType) {
        recordMetric("EventTriggered_" + eventType, 1);
    }

    @Override
    public void recordTimeInZone(String zoneName, long ticks) {
        recordMetric("TimeInZone_" + zoneName, ticks);
    }

    @Override
    public void recordQueueWait(String zoneName, long ticks) {
        recordMetric("QueueWait_" + zoneName, ticks);
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