package States;

import org.junit.jupiter.api.Test;
import org.simulation.Agent;
import org.simulation.Event;
import sim.field.grid.SparseGrid2D;
import sim.util.Int2D;

import static org.junit.jupiter.api.Assertions.*;

public class PanicRunStateTest {

    @Test
    public void testAgentStartsPanickingOnFirstAct() {
        // Arrange
        Agent agent = new Agent();
        DummyEvent event = new DummyEvent(10, 10);
        Int2D startPos = new Int2D(5, 5);
        event.grid.setObjectLocation(agent, startPos);

        PanicRunState state = new PanicRunState();

        // Act
        IStates nextState = state.act(agent, event);

        // Assert
        assertTrue(agent.isPanicking(), "Agent sollte im Panikmodus sein");
        assertSame(state, nextState,
                "Agent sollte im gleichen Zustand bleiben, wenn Ziel noch nicht erreicht");
    }

    @Test
    public void testAgentMovesTowardsExit() {
        // Arrange
        Agent agent = new Agent();
        DummyEvent event = new DummyEvent(10, 10);
        Int2D startPos = new Int2D(5, 5);
        event.grid.setObjectLocation(agent, startPos);

        PanicRunState state = new PanicRunState();

        // Act
        state.act(agent, event); // erster Schritt
        Int2D newPos = event.grid.getObjectLocation(agent);

        // Assert
        assertEquals(new Int2D(4, 4), newPos, "Agent sollte sich in Richtung (0,0) bewegen");
    }

    @Test
    public void testAgentReachesExitAndStopsPanicking() {
        // Arrange
        Agent agent = new Agent();
        DummyEvent event = new DummyEvent(10, 10);
        Int2D startPos = new Int2D(0, 0); // bereits am Exit

        event.grid.setObjectLocation(agent, startPos);
        PanicRunState state = new PanicRunState();

        // Act
        IStates nextState = state.act(agent, event);

        // Assert
        assertFalse(agent.isPanicking(), "Agent sollte Panikmodus verlassen haben");
        assertInstanceOf(RoamingState.class, nextState, "Agent sollte wieder im RoamingState sein");
    }

    // Dummy Event mit Grid
    public static class DummyEvent extends Event {
        public DummyEvent(int width, int height) {
            super(System.currentTimeMillis());
            this.grid = new SparseGrid2D(width, height);
        }
    }
}
