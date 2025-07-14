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

/**
 * Testklasse für WatchingMainActState, die das Verhalten eines Agenten im WatchingMainActState überprüft.
 *
 * @author Burak Tamer
 */
public class WatchingMainActStateTest {

    private Event event;
    private Agent agent;
    private Zone mainAct;

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
        mainAct = new Zone(Zone.ZoneType.ACT_MAIN, new Int2D(30, 30), 10);
        event.zones.add(mainAct);
        event.grid.setObjectLocation(agent, new Int2D(30, 30));
        agent.setCurrentZone(mainAct);
    }

    @Test
    public void testAgentWatchesActAndReturnsToRoaming() {
        WatchingMainActState state = new WatchingMainActState(event);

        IStates current = state;
        for (int i = 0; i < 500; i++) {
            current = current.act(agent, event);
            if (current instanceof RoamingState) break; // stopper
        }

        assertInstanceOf(RoamingState.class, current);
        assertNull(agent.getCurrentZone());
        assertFalse(agent.isWatchingMain());
    }

    @Test
    void testAgentTrappedInRestrictedArea() {
        // RestrictedArea genau dort platzieren, wo der Agent steht
        Zone actMain = event.getZoneByType(Zone.ZoneType.ACT_MAIN);
        Int2D pos = actMain.getPosition();

        RestrictedArea restrictedArea = new RestrictedArea(pos.x, pos.y, 5);
        restrictedArea.activate();
        event.addRestrictedArea(restrictedArea);
        event.grid.setObjectLocation(restrictedArea, pos);

        WatchingMainActState state = new WatchingMainActState(event);

        IStates resultState = state.act(agent, event);

        // Agent bleibt im gleichen State stecken
        assertInstanceOf(WatchingMainActState.class, resultState, "Agent sollte im WatchingMainActState bleiben");
    }

    @Test
    void testAgentMovesTowardsMainActZone() {
        WatchingMainActState state = new WatchingMainActState(event);

        // init first (sets target)
        state.act(agent, event);

        // versetze Agent woanders hin
        Int2D start = new Int2D(10, 10);
        event.grid.setObjectLocation(agent, start);
        agent.setTargetPosition(mainAct.getPosition());

        IStates result = state.act(agent, event);

        Int2D newPos = event.grid.getObjectLocation(agent);
        assertNotNull(newPos);
        assertNotEquals(start, newPos, "Agent sollte sich in Richtung Ziel bewegen.");
        assertSame(state, result, "Agent sollte im selben State bleiben.");
    }

    @Test
    void testAgentStaysAtSamePositionWhenTrapped() {
        Zone actMain = event.getZoneByType(Zone.ZoneType.ACT_MAIN);
        Int2D pos = actMain.getPosition();

        RestrictedArea restrictedArea = new RestrictedArea(pos.x, pos.y, 5);
        restrictedArea.activate();
        event.addRestrictedArea(restrictedArea);
        event.grid.setObjectLocation(restrictedArea, pos);

        WatchingMainActState state = new WatchingMainActState(event);

        Int2D oldPos = event.grid.getObjectLocation(agent);
        IStates resultState = state.act(agent, event);

        Int2D newPos = event.grid.getObjectLocation(agent);
        assertEquals(oldPos, newPos, "Agent sollte sich nicht bewegen, wenn er gefangen ist.");
        assertInstanceOf(WatchingMainActState.class, resultState, "Agent sollte im WatchingMainActState bleiben.");
    }
}
