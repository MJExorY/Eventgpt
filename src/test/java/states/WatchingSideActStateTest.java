package states;

import metrics.DefaultMetricsCollector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.simulation.Agent;
import org.simulation.Event;
import org.simulation.RestrictedArea;
import sim.util.Int2D;
import zones.Zone;

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
        WatchingSideActState state = new WatchingSideActState(event);

        IStates current = state;
        boolean reachedRoaming = false;

        for (int i = 0; i < 600; i++) {
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

    @Test
    void testBlockedMovementToSideActZone() {
        Zone sideAct = new Zone(Zone.ZoneType.ACT_SIDE, new Int2D(5, 5), 5);
        event.zones.add(sideAct);

        RestrictedArea ra = new RestrictedArea(5, 5, 1);
        event.addRestrictedArea(ra);

        Agent agent2 = new Agent();
        agent2.setEvent(event);
        event.grid.setObjectLocation(agent2, new Int2D(4, 5));

        WatchingSideActState state = new WatchingSideActState(event);
        state.act(agent2, event);

        Int2D pos = event.grid.getObjectLocation(agent2);
        assertNotEquals(new Int2D(5, 5), pos,
                "Agent darf Ziel nicht erreichen, weil blockiert.");
    }
}