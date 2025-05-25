package States;

import org.junit.jupiter.api.Test;
import org.simulation.Agent;
import org.simulation.Event;
import org.simulation.Zone;
import sim.util.Int2D;

import static org.junit.jupiter.api.Assertions.*;

public class HungryThirstyStateTest {

    @Test
    public void testAgentIsHungry_transitionsToSeekingZoneState() {
        // Arrange
        Agent agent = new Agent();
        agent.setHungry(true);

        Int2D foodPos = new Int2D(10, 10);
        Zone foodZone = new Zone(Zone.ZoneType.FOOD, foodPos, 5);

        DummyEvent event = new DummyEvent(foodZone);

        HungryThirstyState state = new HungryThirstyState();

        // Act
        IStates nextState = state.act(agent, event);

        // Assert
        assertInstanceOf(SeekingZoneState.class, nextState);
        assertEquals(foodPos, agent.getTargetPosition(),
                "Agent sollte Position der Essenszone anpeilen");
        assertFalse(agent.isHungry(), "Hungerflag sollte nach Zustandübergang zurückgesetzt sein");
    }

    @Test
    public void testAgentIsNotHungry_remainsInRoamingState() {
        // Arrange
        Agent agent = new Agent(); // default: isHungry = false
        Int2D foodPos = new Int2D(10, 10);
        Zone foodZone = new Zone(Zone.ZoneType.FOOD, foodPos, 5);
        DummyEvent event = new DummyEvent(foodZone);

        HungryThirstyState state = new HungryThirstyState();

        // Act
        IStates nextState = state.act(agent, event);

        // Assert
        assertInstanceOf(RoamingState.class, nextState);
        assertNull(agent.getTargetPosition(), "Target sollte nicht gesetzt werden");
    }

    // DummyEvent ohne Simulation, nur override für Zone-Zugriff
    public static class DummyEvent extends Event {
        private final Zone foodZone;

        public DummyEvent(Zone foodZone) {
            super(System.currentTimeMillis());
            this.foodZone = foodZone;
        }

        @Override
        public Zone getZoneByType(Zone.ZoneType type) {
            if (type == Zone.ZoneType.FOOD) {
                return foodZone;
            }
            return null;
        }
    }
}
