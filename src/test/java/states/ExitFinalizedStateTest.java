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
 * Testklasse für ExitFinalizedState, welche das Verhalten
 * eines Agenten beim gezielten Verlassen der Simulation über einen
 * Notausgang überprüft.
 *
 * @author Gerardo Carnevale
 */
public class ExitFinalizedStateTest {

    private Event event;
    private Agent agent;
    private ExitFinalizedState state;
    private Zone exitZone;

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

        exitZone = new Zone(Zone.ZoneType.EMERGENCY_EXIT, new Int2D(5, 5), 1);
        event.zones.add(exitZone);

        agent = new Agent();
        agent.setEvent(event);
        event.agents.add(agent);
        event.grid.setObjectLocation(agent, new Int2D(0, 0));

        state = new ExitFinalizedState();
    }

    @Test
    public void testAgentMovesTowardsExit() {
        IStates next = state;
        for (int i = 0; i < 5; i++) {
            next = next.act(agent, event);
            assertNotNull(agent.getTargetPosition(), "Agent sollte ein Ziel haben");
        }
    }

    @Test
    public void testAgentTransitionsToQueueWhenExitFull() {
        Agent blocker = new Agent();
        blocker.setEvent(event);
        event.agents.add(blocker);
        event.grid.setObjectLocation(blocker, exitZone.getPosition());
        exitZone.enter(blocker);

        IStates next = state;
        boolean enteredQueue = false;
        for (int i = 0; i < 50; i++) {
            next = next.act(agent, event);
            if (next instanceof QueueingState) {
                enteredQueue = true;
                break;
            }
        }
        assertTrue(enteredQueue, "Agent sollte in den QueueingState wechseln");
    }

    @Test
    public void testAgentEntersExitWhenNotFull() {
        IStates next = state;
        boolean reachedExit = false;
        for (int i = 0; i < 50; i++) {
            next = next.act(agent, event);
            if (next == null) {
                reachedExit = true;
                break;
            }
        }
        assertTrue(reachedExit, "Agent sollte in den Exit eintreten und entfernt werden");
    }

    @Test
    public void testTargetSetOnFirstAct() {
        assertFalse(getPrivateTargetSet(state), "targetSet sollte false sein");
        state.act(agent, event);
        assertTrue(getPrivateTargetSet(state), "targetSet sollte nach act true sein");
    }

    // Helfermethode zum Zugriff auf privates Feld (Reflection, für Testzwecke)
    private boolean getPrivateTargetSet(ExitFinalizedState s) {
        try {
            java.lang.reflect.Field f = s.getClass().getDeclaredField("targetSet");
            f.setAccessible(true);
            return f.getBoolean(s);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
