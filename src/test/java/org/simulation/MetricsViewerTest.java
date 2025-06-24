package org.simulation;

import metrics.DefaultMetricsCollector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sim.util.Int2D;

import static org.junit.jupiter.api.Assertions.*;

public class MetricsViewerTest {

    private DefaultMetricsCollector collector;

    @BeforeEach
    void setUp() {
        collector = new DefaultMetricsCollector();
    }

    @Test
    void testZoneEntrySummaryInReport() {
        collector.recordZoneEntry(new Agent(), new Zone(Zone.ZoneType.FOOD, new Int2D(1, 1), 5));
        collector.recordZoneEntry(new Agent(), new Zone(Zone.ZoneType.FOOD, new Int2D(2, 2), 5));
        collector.recordZoneEntry(new Agent(), new Zone(Zone.ZoneType.WC, new Int2D(3, 3), 5));

        String report = MetricsViewer.generateReport(collector);

        assertTrue(report.contains("FOOD"));
        assertTrue(report.contains("WC"));
        assertTrue(report.contains("➤")); // Gesamtzeile
    }

    @Test
    void testQueueWaitSectionInReport() {
        collector.recordQueueWait("FOOD", 60);  // 1 min
        collector.recordQueueWait("FOOD", 120); // 2 min

        String report = MetricsViewer.generateReport(collector);

        assertTrue(report.contains("QueueWait_FOOD") || report.contains("FOOD"));
        assertTrue(report.contains("Ø"));
        assertTrue(report.contains("max"));
    }

    @Test
    void testTimeInZoneSectionInReport() {
        collector.recordTimeInZone("ACT_MAIN", 30);
        collector.recordTimeInZone("ACT_MAIN", 90);

        String report = MetricsViewer.generateReport(collector);

        assertTrue(report.contains("ACT_MAIN"));
        assertTrue(report.contains("Ø"));
        assertTrue(report.contains("max"));
    }

    @Test
    void testEventIconsInReport() {
        collector.recordEventTriggered("FIRE");
        collector.recordEventTriggered("FIGHT");
        collector.recordEventTriggered("STORM");
        collector.recordEventTriggered("ALIEN");

        String report = MetricsViewer.generateReport(collector);

        assertTrue(report.contains("🔥"));
        assertTrue(report.contains("🥊"));
        assertTrue(report.contains("🌩"));
        assertTrue(report.contains("❔")); // Unbekanntes Event
    }

    @Test
    void testEmptyCollectorProducesValidReport() {
        String report = MetricsViewer.generateReport(collector);
        assertNotNull(report);
        assertTrue(report.contains("Simulation beendet"));
    }
}
