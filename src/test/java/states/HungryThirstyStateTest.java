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
 * Testklasse für das Verhalten von HungryThirstyState.
 * Simuliert Agenten, die hungrig oder durstig sind und ein Ziel (z. B. FoodZone) ansteuern.
 *
 * @author cb-235866
 */
public class HungryThirstyStateTest {

    private Event event;
    private Agent agent;

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

        Zone foodZone = new Zone(Zone.ZoneType.FOOD, new Int2D(5, 15), 5);
        event.zones.add(foodZone);
        event.grid.setObjectLocation(agent, new Int2D(0, 0));
    }

    @Test
    public void testAgentReachesFoodAndReturnsToRoaming() {
        HungryThirstyState state = new HungryThirstyState(event);
        IStates result = state.act(agent, event);

        assertTrue(agent.isHungry());
        assertEquals(new Int2D(5, 15), agent.getTargetPosition());

        // Agent erreicht die Zone
        event.grid.setObjectLocation(agent, new Int2D(5, 15));

        // simulate several ticks to wait inside zone
        for (int i = 0; i < 200; i++) {
            result = state.act(agent, event);
            if (result instanceof RoamingState) break;  // early exit if already returned
        }

        assertInstanceOf(RoamingState.class, result);
    }
}
