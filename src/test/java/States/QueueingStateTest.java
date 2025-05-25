package States;

import org.junit.jupiter.api.Test;
import org.simulation.Agent;
import org.simulation.Event;
import sim.field.grid.SparseGrid2D;
import sim.util.Int2D;

import static org.junit.jupiter.api.Assertions.*;

public class QueueingStateTest {

    @Test
    public void testFirstActInitializesQueueing() {
        // Arrange
        Agent agent = new Agent();
        DummyEvent event = new DummyEvent(10, 10);
        Int2D position = new Int2D(3, 3);
        event.grid.setObjectLocation(agent, position);

        QueueingState state = new QueueingState(3); // custom ctor with fixed time

        // Act
        IStates nextState = state.act(agent, event);

        // Assert
        assertTrue(agent.isInQueue(), "Agent sollte in der Warteschlange sein");
        assertEquals(position, event.grid.getObjectLocation(agent),
                "Agent sollte sich nicht bewegen");
        assertSame(state, nextState, "Zustand sollte beim ersten Aufruf gleich bleiben");
    }

    @Test
    public void testWaitsUntilTimeExpires() {
        // Arrange
        Agent agent = new Agent();
        DummyEvent event = new DummyEvent(10, 10);
        Int2D position = new Int2D(5, 5);
        event.grid.setObjectLocation(agent, position);

        QueueingState state = new QueueingState(2);

        // Act
        IStates stateAfterFirst = state.act(agent, event); // t = 1
        assertSame(state, stateAfterFirst);
        IStates stateAfterSecond = state.act(agent, event); // t = 0

        // Assert
        assertFalse(agent.isInQueue(), "Agent sollte Warteschlange verlassen haben");
        assertInstanceOf(WatchingActState.class, stateAfterSecond,
                "Nach Ablauf Wechsel in WatchingActState");
    }

    // ===== Dummy-Hilfsklassen =====

    public static class DummyEvent extends Event {
        public DummyEvent(int width, int height) {
            super(System.currentTimeMillis());
            this.grid = new SparseGrid2D(width, height);
        }
    }

    // === Erweiterte Klasse mit fixierter Wartezeit für Testbarkeit ===
    public static class QueueingState extends States.QueueingState {
        public QueueingState(int fixedTime) {
            super();
            this.waitingTime = fixedTime;
        }
    }
}
