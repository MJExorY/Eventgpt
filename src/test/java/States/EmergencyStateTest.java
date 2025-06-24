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
        dummyEvent = new Event(System.currentTimeMillis(), 15, 15, 15, null);
        dummyEvent.grid = new sim.field.grid.SparseGrid2D(100, 100);
    }

    @Test
    void testMedicMovesTowardAssignedTarget() {
        Person medic = new Person(Person.PersonType.MEDIC);
        medic.setEvent(dummyEvent);
        dummyEvent.grid.setObjectLocation(medic, new Int2D(5, 5));

        // Zielposition zuweisen (z. B. FightDisturbance bei (10,10))
        Int2D target = new Int2D(10, 10);
        medic.setTargetPosition(target);

        IStates emergencyState = new EmergencyState();
        IStates nextState = emergencyState.act(medic, dummyEvent);

        Int2D newPos = dummyEvent.grid.getObjectLocation(medic);
        assertNotNull(newPos, "MEDIC position should not be null after movement");

        // Überprüfen, ob sich der MEDIC dem Ziel angenähert hat
        int oldDistance = Math.abs(5 - target.x) + Math.abs(5 - target.y);
        int newDistance = Math.abs(newPos.x - target.x) + Math.abs(newPos.y - target.y);

        assertTrue(newDistance < oldDistance, "MEDIC should move closer to the target");
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