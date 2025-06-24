package States;

import metrics.DefaultMetricsCollector;
import metrics.MetricsCollector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.simulation.Agent;
import org.simulation.Event;
import org.simulation.Zone;
import sim.util.Int2D;

import static org.junit.jupiter.api.Assertions.*;

public class WatchingSideActStateTest {

    private Event event;
    private Agent agent;
    private Zone sideActZone;

    @BeforeEach
    public void setUp() {
        DefaultMetricsCollector collector = new DefaultMetricsCollector();
        for (Zone.ZoneType type : Zone.ZoneType.values()) {
            collector.registerMetric("ZoneEntry_" + type);
            collector.registerMetric("ZoneExit_" + type);
            collector.registerMetric("PanicEscape_" + type);
        }

        event = new Event(System.currentTimeMillis(), 0, 0, 0, collector);
        event.start();

        agent = new Agent();
        agent.setEvent(event);

        // SideAct-Zone erstellen und hinzufügen
        sideActZone = new Zone(Zone.ZoneType.ACT_SIDE, new Int2D(10, 10), 10);
        event.zones.add(sideActZone);

        // Agent startet außerhalb der Zone
        event.grid.setObjectLocation(agent, new Int2D(0, 0));
        // KEIN agent.setCurrentZone(...)!
    }

    @Test
    public void testAgentWatchesSideActAndReturnsToRoaming() {
        WatchingSideActState state = new WatchingSideActState();

        IStates current = state;
        boolean reachedRoaming = false;

        for (int i = 0; i < 300; i++) {
            current = current.act(agent, event);
            if (current instanceof RoamingState) {
                reachedRoaming = true;
                break;
            }
        }

        assertTrue(reachedRoaming, "Agent sollte nach Zuschauen in RoamingState wechseln");
        assertNull(agent.getCurrentZone(), "Agent sollte Zone verlassen haben");
        assertFalse(agent.isWatchingSide(), "Agent sollte WatchingSide-Flag zurückgesetzt haben");
    }
}