package States;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.simulation.Agent;
import org.simulation.Event;
import org.simulation.Zone;
import sim.util.Int2D;

import static org.junit.jupiter.api.Assertions.*;

public class WatchingMainActStateTest {

    private Event event;
    private Agent agent;
    private Zone mainAct;

    @BeforeEach
    public void setUp() {
        event = new Event(System.currentTimeMillis(), 0, 0, 0);
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
        WatchingMainActState state = new WatchingMainActState();

        IStates current = state;
        for (int i = 0; i < 100; i++) {
            current = current.act(agent, event);
            if (current instanceof RoamingState) break; // stopper
        }

        assertInstanceOf(RoamingState.class, current);
        assertNull(agent.getCurrentZone());
        assertFalse(agent.isWatchingMain());
    }
}
