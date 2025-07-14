package org.simulation;

import metrics.SimpleMetricsCollector;
import states.RoamingState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import states.EmergencyState;
import sim.util.Int2D;
import org.simulation.utils.MovementUtils;
import zones.Zone;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-Tests für die Person-Klasse.
 * Testet Verhalten, Farben, Zustände und Bewegungslogik abhängig vom Personentyp.
 *
 * @author Dorothea Ziegler
 */

public class PersonTest {
    private Event event;

    @BeforeEach
    void setUp() {
        event = new Event(System.currentTimeMillis(), 0, 0, 0, new SimpleMetricsCollector());
        event.start();
    }

    @Test
    void testMedicStartsWithEmergencyState() {
        Person medic = new Person(Person.PersonType.MEDIC);
        assertNotNull(medic.getCurrentState());
        assertInstanceOf(EmergencyState.class, medic.getCurrentState());
    }

    @Test
    void testSecurityStartsWithEmergencyState() {
        Person security = new Person(Person.PersonType.SECURITY);
        assertNotNull(security.getCurrentState());
        assertInstanceOf(EmergencyState.class, security.getCurrentState());
    }

    @Test
    void testVisitorStartsWithRoamingState() {
        Person visitor = new Person(Person.PersonType.VISITOR);
        assertNotNull(visitor.getCurrentState());
        assertInstanceOf(RoamingState.class, visitor.getCurrentState());
    }

    @Test
    void testMedicColor() {
        Person medic = new Person(Person.PersonType.MEDIC);
        assertEquals(Color.WHITE, medic.getColor());
    }

    @Test
    void testSecurityColor() {
        Person security = new Person(Person.PersonType.SECURITY);
        assertEquals(Color.DARK_GRAY, security.getColor());
    }

    @Test
    void testVisitorColor() {
        Person visitor = new Person(Person.PersonType.VISITOR);
        assertEquals(Color.YELLOW, visitor.getColor());
    }

    @Test
    void testPersonReachesExit() {
        Person person = new Person(Person.PersonType.VISITOR);
        person.setEvent(event);

        Int2D exitPos = new Int2D(60, 90);
        Zone exitZone = new Zone(Zone.ZoneType.EXIT, exitPos, Integer.MAX_VALUE);
        event.zones.add(exitZone);
        event.grid.setObjectLocation(exitZone, exitPos);

        event.grid.setObjectLocation(person, exitPos);

        // simuliere ein Stoppable
        person.setStopper(event.schedule.scheduleRepeating(person));

        // Person sollte sich beim Step entfernen
        person.step(event);

        assertFalse(event.agents.contains(person), "Person sollte nach Erreichen des Exits entfernt werden");
    }

    @Test
    void testPersonReachesEmergencyExitWhilePanicking() {
        Person person = new Person(Person.PersonType.VISITOR);
        person.setPanicking(true);
        person.setEvent(event);

        Int2D emergencyExitPos = new Int2D(50, 5);
        Zone emergencyZone = new Zone(Zone.ZoneType.EMERGENCY_EXIT, emergencyExitPos, Integer.MAX_VALUE);
        event.zones.add(emergencyZone);
        event.grid.setObjectLocation(emergencyZone, emergencyExitPos);
        event.grid.setObjectLocation(person, emergencyExitPos);

        person.setStopper(event.schedule.scheduleRepeating(person));

        person.step(event);

        assertFalse(event.agents.contains(person), "Panische Person sollte Emergency Exit verlassen und entfernt werden");
    }

    @Test
    void testPersonRandomMoveWhenNoTarget() {
        Person person = new Person(Person.PersonType.VISITOR);
        person.setEvent(event);

        Int2D startPos = new Int2D(20, 20);
        event.grid.setObjectLocation(person, startPos);
        event.agents.add(person);

// **NUR randomMove direkt aufrufen**
        MovementUtils.randomMove(person, event);

        Int2D newPos = event.grid.getObjectLocation(person);
        assertNotNull(newPos, "Person sollte nach randomMove noch im Grid sein.");

        int dx = Math.abs(newPos.x - startPos.x);
        int dy = Math.abs(newPos.y - startPos.y);

        assertTrue(dx <= 1 && dy <= 1, "Random Move darf maximal 1 Zelle entfernt sein.");

    }

    @Test
    void testPersonMovesTowardTarget() {
        Person person = new Person(Person.PersonType.VISITOR);
        person.setEvent(event);

        Int2D startPos = new Int2D(10, 10);
        Int2D targetPos = new Int2D(15, 15);
        event.grid.setObjectLocation(person, startPos);
        person.setTargetPosition(targetPos);
        event.agents.add(person);

        // Statt RoamingState → gezielten State setzen:
        person.setCurrentState(new EmergencyState());

        person.step(event);

        Int2D newPos = event.grid.getObjectLocation(person);
        assertNotNull(newPos);

        int oldDist = Math.abs(startPos.x - targetPos.x) + Math.abs(startPos.y - targetPos.y);
        int newDist = Math.abs(newPos.x - targetPos.x) + Math.abs(newPos.y - targetPos.y);

        assertTrue(newDist < oldDist,
                String.format("Person sollte sich dem Ziel annähern. Start: %s, Neu: %s, Target: %s",
                        startPos, newPos, targetPos));
    }


}

