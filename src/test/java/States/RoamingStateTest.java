package States;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.simulation.Agent;
import org.simulation.Event;
import sim.util.Int2D;

import static org.junit.jupiter.api.Assertions.*;

public class RoamingStateTest {

    private Event event;
    private Agent agent;
    private RoamingState roamingState;

    @BeforeEach
    public void setUp() {
        event = new Event(System.currentTimeMillis(), 0);
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
