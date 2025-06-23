package States;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.simulation.Agent;
import org.simulation.Event;
import org.simulation.Zone;
import sim.util.Int2D;

import static org.junit.jupiter.api.Assertions.*;

public class QueueingStateTest {

    private Event event;
    private Agent agent;
    private Zone testZone;

    @BeforeEach
    public void setUp() {
        event = new Event(System.currentTimeMillis(), 0, 0, 0);
        event.start();

        agent = new Agent();
        agent.setEvent(event);

        testZone = new Zone(Zone.ZoneType.FOOD, new Int2D(5, 5), 1);
        event.zones.add(testZone);
        event.grid.setObjectLocation(agent, new Int2D(0, 0));
    }

    @Test
    public void testAgentEventuallyEntersZone() {
        HungryThirstyState followUpState = new HungryThirstyState();
        QueueingState state = new QueueingState(agent, testZone, followUpState);

        IStates result = state;

        // Simuliere mehrere Schritte bis Agent die Zone betritt
        boolean entered = false;
        for (int i = 0; i < 50; i++) {
            result = result.act(agent, event);

            if (result != state) {
                entered = true;
                break;
            }
        }

        assertTrue(entered, "Agent sollte irgendwann die Zone betreten.");
        assertEquals(followUpState.getClass(), result.getClass());
        assertFalse(agent.isInQueue());
    }

    @Test
    public void testAgentGivesUpAfterMaxRetries() {
        // Besetze Zone dauerhaft, sodass der Agent sie nie betreten kann
        Agent blocker = new Agent();
        blocker.setEvent(event);
        testZone.enter(blocker);

        QueueingState state = new QueueingState(agent, testZone, new HungryThirstyState());

        IStates result = state;

        for (int i = 0; i < 100; i++) {
            result = result.act(agent, event);
            if (result instanceof RoamingState) break;
        }

        assertInstanceOf(RoamingState.class, result);
        assertFalse(agent.isInQueue());
    }

    @Test
    public void testInitialQueueFlagsSet() {
        QueueingState state = new QueueingState(agent, testZone, new HungryThirstyState());

        // Flags erst nach erstem act() gesetzt
        assertFalse(agent.isInQueue());

        state.act(agent, event);
        assertTrue(agent.isInQueue());
    }
}