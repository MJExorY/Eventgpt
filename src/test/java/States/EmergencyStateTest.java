package States;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.simulation.Event;
import org.simulation.Person;
import sim.util.Int2D;

import static org.junit.jupiter.api.Assertions.*;

public class EmergencyStateTest {

    private Event dummyEvent;

    @BeforeEach
    void setup() {
        dummyEvent = new Event(System.currentTimeMillis());
        dummyEvent.grid = new sim.field.grid.SparseGrid2D(100, 100);
    }

    @Test
    void testMedicMovesTowardTarget() {
        Person medic = new Person(Person.PersonType.MEDIC);
        medic.setEvent(dummyEvent);
        dummyEvent.grid.setObjectLocation(medic, new Int2D(5, 5));

        IStates emergencyState = new EmergencyState();
        IStates nextState = emergencyState.act(medic, dummyEvent);

        Int2D newPos = dummyEvent.grid.getObjectLocation(medic);
        assertNotNull(newPos);
        assertTrue(newPos.x > 5 || newPos.y > 5, "MEDIC should move towards target (10,10)");
        assertEquals(emergencyState, nextState);
    }

    @Test
    void testSecurityRandomMovement() {
        Person security = new Person(Person.PersonType.SECURITY);
        security.setEvent(dummyEvent);
        dummyEvent.grid.setObjectLocation(security, new Int2D(50, 50));

        IStates emergencyState = new EmergencyState();
        IStates nextState = emergencyState.act(security, dummyEvent);

        Int2D newPos = dummyEvent.grid.getObjectLocation(security);
        assertNotNull(newPos);
        assertTrue(Math.abs(newPos.x - 50) <= 1 && Math.abs(newPos.y - 50) <= 1,
                "SECURITY should have moved randomly by at most 1 cell");
        assertEquals(emergencyState, nextState);
    }
}