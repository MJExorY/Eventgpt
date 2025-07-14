package states;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.simulation.Event;
import org.simulation.Person;

import sim.util.Int2D;
import zones.Zone;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testklasse für die EmergencyState-Logik von Person-Instanzen
 * wie MEDIC oder SECURITY im Notfallmodus.
 *
 * @author Dorothea Ziegler
 */
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

        // Zielposition zuweisen (z. B. FightDisturbance bei (10,10))
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

    @Test
    void testSecurityMovesTowardAssignedTarget() {
        Person security = new Person(Person.PersonType.SECURITY);
        security.setEvent(dummyEvent);
        dummyEvent.grid.setObjectLocation(security, new Int2D(50, 50));

        Int2D target = new Int2D(55, 55);
        security.setTargetPosition(target);

        IStates emergencyState = new EmergencyState();
        IStates nextState = emergencyState.act(security, dummyEvent);

        Int2D newPos = dummyEvent.grid.getObjectLocation(security);
        assertNotNull(newPos);
        int oldDistance = Math.abs(50 - target.x) + Math.abs(50 - target.y);
        int newDistance = Math.abs(newPos.x - target.x) + Math.abs(newPos.y - target.y);

        assertTrue(newDistance < oldDistance, "SECURITY should move closer to target.");
        assertEquals(emergencyState, nextState);
    }

    @Test
    void testAgentEntersZone() {
        Person medic = new Person(Person.PersonType.MEDIC);
        medic.setEvent(dummyEvent);
        Int2D pos = new Int2D(10, 10);
        dummyEvent.grid.setObjectLocation(medic, pos);

        Zone zone = new Zone(Zone.ZoneType.FOOD, pos, 10);
        dummyEvent.zones.add(zone);

        // Spy das tryEnterZone, damit wir wissen, ob es aufgerufen wurde
        medic = new Person(medic.getType()) {
            @Override
            public boolean tryEnterZone(Zone z) {
                assertEquals(zone, z, "Agent should try to enter the correct zone");
                return true;
            }
        };
        medic.setEvent(dummyEvent);
        dummyEvent.grid.setObjectLocation(medic, pos);

        EmergencyState emergencyState = new EmergencyState();
        emergencyState.act(medic, dummyEvent);
    }

    @Test
    void testVisitorDoesNothingInEmergencyState() {
        Person visitor = new Person(Person.PersonType.VISITOR);
        visitor.setEvent(dummyEvent);
        dummyEvent.grid.setObjectLocation(visitor, new Int2D(20, 20));

        EmergencyState emergencyState = new EmergencyState();
        IStates nextState = emergencyState.act(visitor, dummyEvent);

        assertEquals(emergencyState, nextState);
    }


}