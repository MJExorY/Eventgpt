package metrics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.simulation.Agent;
import zones.Zone;
import sim.util.Int2D;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testklasse für DefaultMetricsCollector.
 * Stellt sicher, dass Metriken korrekt gesammelt, gespeichert und ausgegeben werden.
 *
 * @author cb-235866
 */

class DefaultMetricsCollectorTest {

    private DefaultMetricsCollector collector;
    private Agent agent;
    private Zone zone;

    @BeforeEach
    void setUp() {
        collector = new DefaultMetricsCollector();
        agent = new Agent();
        zone = new Zone(Zone.ZoneType.FOOD, new Int2D(5, 5), 10);
    }

    @Test
    void testRecordMetricZoneEntry() {
        collector.recordZoneEntry(agent, zone);
        Map<String, List<Object>> metrics = collector.getAllMetrics();
        assertTrue(metrics.containsKey("ZoneEntry_FOOD"));
        assertEquals(1, metrics.get("ZoneEntry_FOOD").size());
    }

    @Test
    void testRecordMetricZoneExit() {
        collector.recordZoneExit(agent, zone);
        Map<String, List<Object>> metrics = collector.getAllMetrics();
        assertTrue(metrics.containsKey("ZoneExit_FOOD"));
        assertEquals(1, metrics.get("ZoneExit_FOOD").size());
    }

    @Test
    void testRecordMetricPanicEscape() {
        agent.setPanicTicks(120); // 2 Minuten
        collector.recordPanicEscape(agent, zone);
        Map<String, List<Object>> metrics = collector.getAllMetrics();
        assertTrue(metrics.containsKey("PanicDuration"));
        assertEquals(1, metrics.get("PanicDuration").size());
        assertEquals(2.0, metrics.get("PanicDuration").get(0));
    }

    @Test
    void testRecordMetricEventTriggered() {
        collector.recordEventTriggered("ALARM");
        Map<String, List<Object>> metrics = collector.getAllMetrics();
        assertTrue(metrics.containsKey("EventTriggered_ALARM"));
        assertEquals(1, metrics.get("EventTriggered_ALARM").size());
        assertEquals(1, metrics.get("EventTriggered_ALARM").get(0));
    }

    @Test
    void testRecordMetricTimeInZone() {
        collector.recordTimeInZone("WC", 300);
        Map<String, List<Object>> metrics = collector.getAllMetrics();
        assertTrue(metrics.containsKey("TimeInZone_WC"));
        assertEquals(1, metrics.get("TimeInZone_WC").size());
        assertEquals(300L, metrics.get("TimeInZone_WC").get(0));
    }

    @Test
    void testRecordMetricQueueWait() {
        collector.recordQueueWait("FOOD", 90);
        Map<String, List<Object>> metrics = collector.getAllMetrics();
        assertTrue(metrics.containsKey("QueueWait_FOOD"));
        assertEquals(1, metrics.get("QueueWait_FOOD").size());
        assertEquals(90L, metrics.get("QueueWait_FOOD").get(0));
    }

    @Test
    void testPrintZoneEntrySummary() {
        collector.recordZoneEntry(agent, new Zone(Zone.ZoneType.FOOD, new Int2D(1, 1), 5));
        collector.recordZoneEntry(agent, new Zone(Zone.ZoneType.FOOD, new Int2D(2, 2), 5));
        collector.recordZoneEntry(agent, new Zone(Zone.ZoneType.WC, new Int2D(3, 3), 5));

        System.out.println("\n=== Zone Entry Summary Output ===");
        collector.printZoneEntrySummary(); // Output wird nur sichtbar bei manueller Ausführung
    }
}
