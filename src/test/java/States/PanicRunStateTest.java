package States;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.simulation.Agent;
import org.simulation.Event;
import org.simulation.Zone;
import sim.field.grid.SparseGrid2D;
import sim.util.Int2D;

import static org.junit.jupiter.api.Assertions.*;

public class PanicRunStateTest {

    private DummyEvent event;
    private Agent agent;
    private PanicRunState state;

    @BeforeEach
    public void setup() {
        event = new DummyEvent(20, 20);
        agent = new Agent();
        agent.setEvent(event);
        event.grid.setObjectLocation(agent, new Int2D(10, 10));
        state = new PanicRunState();

        // Exit-Zone an Position (0,0) hinzufügen
        Zone exit = new Zone(Zone.ZoneType.EXIT, new Int2D(0, 0), Integer.MAX_VALUE);
        event.zones.add(exit);
        event.grid.setObjectLocation(exit, 0, 0);
    }

    @Test
    public void testAgentEntersPanicState() {
        IStates next = state.act(agent, event);
        assertTrue(agent.isPanicking(), "Agent sollte in Panik sein.");
        assertEquals(state, next, "Agent bleibt im PanicRunState, solange Exit nicht erreicht.");
    }

    @Test
    public void testAgentMovesTowardExit() {
        Int2D before = event.grid.getObjectLocation(agent);
        state.act(agent, event);
        Int2D after = event.grid.getObjectLocation(agent);

        assertNotEquals(before, after, "Agent sollte sich beim ersten Schritt bewegen.");
        assertTrue(after.x < before.x || after.y < before.y,
                "Agent sollte sich Richtung (0,0) bewegen.");
    }

    @Test
    public void testAgentReachesExitAndStopsPanicking() {
        // Arrange
        Agent agent = new Agent();
        DummyEvent event = new DummyEvent(10, 10);
        Int2D exitPos = new Int2D(0, 0);

        // Exit-Zone anlegen und registrieren
        Zone exitZone = new Zone(Zone.ZoneType.EXIT, exitPos, Integer.MAX_VALUE);
        event.zones.add(exitZone);
        event.grid.setObjectLocation(exitZone, exitPos.x, exitPos.y);

        // Agent an die Exit-Position setzen
        event.grid.setObjectLocation(agent, exitPos);

        PanicRunState state = new PanicRunState();

        // Act
        IStates nextState = state.act(agent, event);

        // Assert
        assertFalse(agent.isPanicking(), "Agent sollte Panikmodus verlassen haben");
        assertInstanceOf(RoamingState.class, nextState, "Agent sollte wieder im RoamingState sein");
    }


    // Dummy-Event mit Grid
    private static class DummyEvent extends Event {
        public DummyEvent(int width, int height) {
            super(System.currentTimeMillis());
            this.grid = new SparseGrid2D(width, height);
        }
    }
}
