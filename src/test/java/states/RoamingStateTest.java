package states;

import metrics.DefaultMetricsCollector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.simulation.Agent;
import org.simulation.Event;
import zones.Zone;
import sim.util.Int2D;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testklasse für RoamingState, die das Verhalten eines Agenten im Roaming überprüft.
 *
 * @author cb-235866
 */
public class RoamingStateTest {

    private Event event;
    private Agent agent;
    private RoamingState roamingState;

    @BeforeEach
    public void setUp() {
        DefaultMetricsCollector collector = new DefaultMetricsCollector();
        for (Zone.ZoneType type : Zone.ZoneType.values()) {
            collector.registerMetric("ZoneEntry_" + type);
            collector.registerMetric("ZoneExit_" + type);
            collector.registerMetric("PanicEscape_" + type);
        }

        event = new Event(System.currentTimeMillis(), 0, 0, 0, collector);

        event.random.setSeed(12345L);

        event.start();

        agent = new Agent();
        agent.setEvent(event);

        // Setze Startposition
        Int2D start = new Int2D(50, 50);
        event.grid.setObjectLocation(agent, start);

        roamingState = new RoamingState();
    }

    @Test
    public void testAgentStaysInRoamingByDefault() {
        // Simuliere viele Schritte, bis kein Wechsel passiert
        for (int i = 0; i < 100; i++) {
            IStates result = roamingState.act(agent, event);
            assertNotNull(result);
            assertTrue(result instanceof RoamingState
                    || result instanceof HungryThirstyState
                    || result instanceof WCState
                    || result instanceof WatchingMainActState
                    || result instanceof WatchingSideActState);
        }
    }

    @Test
    public void testAgentMovesInGrid() {
        Int2D before = event.grid.getObjectLocation(agent);
        roamingState.act(agent, event);
        Int2D after = event.grid.getObjectLocation(agent);

        // Agent muss sich entweder bewegen oder auf der Stelle bleiben (bei dx=0, dy=0)
        assertNotNull(after);
        assertTrue(Math.abs(after.x - before.x) <= 1);
        assertTrue(Math.abs(after.y - before.y) <= 1);
    }

    @Test
    public void testRoamingFlagsSet() {
        roamingState.act(agent, event);
        assertTrue(agent.isRoaming());
        assertFalse(agent.isHungry());
        assertFalse(agent.isWatchingMain());
    }
}
