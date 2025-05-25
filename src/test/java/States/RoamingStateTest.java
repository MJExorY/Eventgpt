package States;

import org.junit.jupiter.api.Test;
import org.simulation.Agent;
import org.simulation.Event;
import org.simulation.Zone;
import sim.field.grid.SparseGrid2D;
import sim.util.Int2D;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RoamingStateTest {

    @Test
    public void testAgentMovesWithinGridBoundsAndStaysInRoaming() {
        // Arrange
        Agent agent = new Agent();
        TestEvent event = new TestEvent(10, 10, 1234); // fester Seed
        Int2D startPos = new Int2D(5, 5);
        event.grid.setObjectLocation(agent, startPos);

        RoamingState state = new RoamingState();

        // Act
        IStates resultState = state.act(agent, event);
        Int2D newPos = event.grid.getObjectLocation(agent);

        // Assert
        assertSame(state, resultState, "Agent sollte im RoamingState bleiben");
        assertNotNull(newPos);
        assertTrue(Math.abs(newPos.x - startPos.x) <= 1, "x darf max ±1 abweichen");
        assertTrue(Math.abs(newPos.y - startPos.y) <= 1, "y darf max ±1 abweichen");
    }

    @Test
    public void testTransitionToSeekingZoneStateWhenZoneAvailable() {
        // Arrange
        Agent agent = new Agent();
        agent.setLastVisitedZone(Zone.ZoneType.EXIT); // EXIT wird ausgeschlossen

        TestEvent event = new TestEvent(10, 10, 5678);
        Int2D startPos = new Int2D(2, 2);
        event.grid.setObjectLocation(agent, startPos);

        // Füge alle Zonen hinzu außer EXIT
        event.addZone(new Zone(Zone.ZoneType.FOOD, new Int2D(1, 1), 5));
        event.addZone(new Zone(Zone.ZoneType.ACT_MAIN, new Int2D(2, 2), 5));
        event.addZone(new Zone(Zone.ZoneType.ACT_SIDE, new Int2D(3, 3), 5));

        RoamingState state = new RoamingState();

        // Act
        IStates resultState = state.act(agent, event);

        // Assert
        assertInstanceOf(SeekingZoneState.class, resultState,
                "Agent sollte zu einer Zone wechseln");
        assertNotNull(agent.getTargetPosition(), "Zielposition sollte gesetzt sein");
    }


    // ========== Dummy-Event-Klasse mit kontrollierbarem Zufall ==========

    public static class TestEvent extends Event {
        public TestEvent(int width, int height, long seed) {
            super(seed);
            this.grid = new SparseGrid2D(width, height);
        }

        public void addZone(Zone zone) {
            this.grid.setObjectLocation(zone, zone.getPosition());
            this.getZones().add(zone);
        }

        // Zugriff auf private Liste der Zonen (alternativ Zone als protected machen)
        public List<Zone> getZones() {
            try {
                var field = Event.class.getDeclaredField("zones");
                field.setAccessible(true);
                return (List<Zone>) field.get(this);
            } catch (Exception e) {
                throw new RuntimeException("Zonen konnten nicht gesetzt werden", e);
            }
        }
    }
}
