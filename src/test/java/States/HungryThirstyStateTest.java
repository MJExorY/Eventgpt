package States;

import States.HungryThirstyState;
import States.IStates;
import States.RoamingState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.simulation.Agent;
import org.simulation.Event;
import org.simulation.Zone;
import sim.util.Int2D;

import static org.junit.jupiter.api.Assertions.*;

public class HungryThirstyStateTest {

    private Event event;
    private Agent agent;

    @BeforeEach
    public void setUp() {
        event = new Event(System.currentTimeMillis(), 0, 0, 0);
        event.start();

        agent = new Agent();
        agent.setEvent(event);

        Zone foodZone = new Zone(Zone.ZoneType.FOOD, new Int2D(5, 15), 5);
        event.zones.add(foodZone);
        event.grid.setObjectLocation(agent, new Int2D(0, 0));
    }

    @Test
    public void testAgentReachesFoodAndReturnsToRoaming() {
        HungryThirstyState state = new HungryThirstyState();
        IStates result = state.act(agent, event);

        assertTrue(agent.isHungry());
        assertEquals(new Int2D(5, 15), agent.getTargetPosition());

        // Agent erreicht die Zone
        event.grid.setObjectLocation(agent, new Int2D(5, 15));

        // simulate several ticks to wait inside zone
        for (int i = 0; i < 30; i++) {
            result = state.act(agent, event);
            if (result instanceof RoamingState) break;  // early exit if already returned
        }

        assertInstanceOf(RoamingState.class, result);
    }
}
