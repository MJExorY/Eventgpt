package events;

import org.simulation.Agent;
import org.simulation.Event;
import org.simulation.Person;
import org.simulation.RestrictedArea;
import states.EmergencyState;
import states.PanicRunState;
import metrics.MetricsCollector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sim.util.Int2D;
import zones.Zone;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FightDisturbanceTest {

    private Event event;
    private FightDisturbance fight;
    private Int2D fightPos;

    @BeforeEach
    void setUp() {
        MetricsCollector collector = new SimpleMetricsCollector();
        event = new Event(System.currentTimeMillis(), 20, 20, 1, collector);
        event.start();
        event.agents.clear();

        fightPos = new Int2D(10, 10);
        fight = new FightDisturbance(fightPos);
        event.grid.setObjectLocation(fight, fightPos);
        fight.getAssignedSecurity().clear();
    }

    @Test
    void testAssignSecurityAndMedic() {
        Person security = new Person(Person.PersonType.SECURITY);
        Person medic = new Person(Person.PersonType.MEDIC);

        security.clearTarget();
        medic.clearTarget();

        event.agents.add(security);
        event.agents.add(medic);

        event.grid.setObjectLocation(security, new Int2D(5, 5));
        event.grid.setObjectLocation(medic, new Int2D(6, 6));

        fight.step(event);

        assertEquals(fightPos, security.getTargetPosition(), "Security sollte auf Fight-Position gesetzt werden");

        assertInstanceOf(EmergencyState.class, security.getCurrentState(), "Security sollte RoamingState haben");

        assertEquals(fightPos, medic.getTargetPosition(), "Medic sollte auf Fight-Position gesetzt werden");
        assertInstanceOf(EmergencyState.class, medic.getCurrentState(), "Medic sollte EmergencyState haben");
    }

    @Test
    void testFightResolutionBySecurityAndMedic() {
        Person security = new Person(Person.PersonType.SECURITY);
        Person medic = new Person(Person.PersonType.MEDIC);

        security.clearTarget();
        medic.clearTarget();

        event.agents.add(security);
        event.agents.add(medic);

        event.grid.setObjectLocation(security, fightPos);
        event.grid.setObjectLocation(medic, fightPos);

        fight.getAssignedSecurity().clear();
        fight.getAssignedSecurity().add(security);

        fight.setAssignedMedic(medic);  // Falls noch kein Setter in FightDisturbance: siehe Hinweis unten

        fight.step(event);

        assertTrue(fight.isResolved(), "Fight sollte nach Security- und Medic-Präsenz als resolved markiert sein");
        assertInstanceOf(EmergencyState.class, security.getCurrentState(), "Security sollte weiterhin EmergencyState haben");
        assertInstanceOf(EmergencyState.class, medic.getCurrentState(), "Medic sollte weiterhin EmergencyState haben");
    }

    @Test
    void testPanicInduction() {
        Agent visitor = new Agent();
        Int2D visitorPos = new Int2D(fightPos.x + 1, fightPos.y);
        event.agents.add(visitor);
        event.grid.setObjectLocation(visitor, visitorPos);

        fight.step(event);

        assertTrue(visitor.isPanicking(), "Besucher in Nähe sollte in Panik geraten");
        assertInstanceOf(PanicRunState.class, visitor.getCurrentState(), "Besucher sollte PanicRunState haben");
    }

    @Test
    void testResolvedFightRemovesAfterTicks() {
        fight.setResolved(true);

        for (int i = 0; i < 4; i++) {
            fight.step(event);
        }

        assertNull(event.grid.getObjectLocation(fight), "Fight sollte nach 4 Ticks aus dem Grid entfernt sein");
    }

    @Test
    void testNoMedicAssigned() {
        Person security = new Person(Person.PersonType.SECURITY);
        event.agents.add(security);
        event.grid.setObjectLocation(security, fightPos);

        fight.getAssignedSecurity().add(security);
        // KEIN Medic gesetzt:
        fight.setAssignedMedic(null);

        fight.step(event);

        // Fight darf nicht resolved sein
        assertFalse(fight.isResolved(), "Fight sollte nicht resolved sein, wenn kein Medic vor Ort ist");
    }

    @Test
    void testNoSecurityPresent() {
        Person medic = new Person(Person.PersonType.MEDIC);
        event.agents.add(medic);

        event.grid.setObjectLocation(medic, fightPos);

        fight.step(event);

        // Fight darf nicht resolved werden
        assertFalse(fight.isResolved(), "Fight sollte nicht resolved werden, wenn keine Security vorhanden ist");
    }

    @Test
    void testDeactivateRestrictedArea() {
        // Arrange
        RestrictedArea ra = new RestrictedArea(fightPos.x, fightPos.y, 8);
        event.addRestrictedArea(ra);
        event.grid.setObjectLocation(ra, fightPos);

        fight.setResolved(true);
        fight.step(event);

        assertFalse(ra.isActive(), "RestrictedArea sollte deaktiviert sein, wenn Fight resolved wurde.");
    }

    @Test
    void testMultipleSecurityPerimeterPositions() {
        Person s1 = new Person(Person.PersonType.SECURITY);
        Person s2 = new Person(Person.PersonType.SECURITY);
        Person s3 = new Person(Person.PersonType.SECURITY);

        event.agents.addAll(List.of(s1, s2, s3));
        event.grid.setObjectLocation(s1, new Int2D(1, 1));
        event.grid.setObjectLocation(s2, new Int2D(2, 2));
        event.grid.setObjectLocation(s3, new Int2D(3, 3));

        fight.step(event);

        assertEquals(fightPos, s1.getTargetPosition(), "Security #1 sollte ins Zentrum laufen.");

        assertNotEquals(fightPos, s2.getTargetPosition(), "Security #2 sollte am Perimeter positioniert werden.");
        assertNotEquals(fightPos, s3.getTargetPosition(), "Security #3 sollte am Perimeter positioniert werden.");
    }

    @Test
    void testBlockedPerimeterPosition() {
        Person security1 = new Person(Person.PersonType.SECURITY);
        Person security2 = new Person(Person.PersonType.SECURITY);

        event.agents.add(security1);
        event.agents.add(security2);

        event.grid.setObjectLocation(security1, new Int2D(5, 5));
        event.grid.setObjectLocation(security2, new Int2D(6, 6));

        // RestrictedArea direkt aktiv
        RestrictedArea ra = new RestrictedArea(6, 18, 1);
        event.addRestrictedArea(ra);

        fight.step(event);

        assertEquals(fightPos, security1.getTargetPosition(), "Security #1 sollte ins Zentrum laufen.");
        assertNotEquals(fightPos, security2.getTargetPosition(), "Security #2 sollte nicht ins Zentrum laufen.");
    }

    @Test
    void testCreateRandomReturnsValidFight() {
        FightDisturbance randomFight = FightDisturbance.createRandom(event);
        assertNotNull(randomFight, "createRandom sollte nie null zurückgeben.");
        Int2D pos = randomFight.getPosition();
        assertNotNull(pos, "Position von randomFight sollte nicht null sein.");
        assertTrue(pos.x >= 0 && pos.x < event.grid.getWidth(), "X-Position muss im Grid liegen.");
        assertTrue(pos.y >= 0 && pos.y < event.grid.getHeight(), "Y-Position muss im Grid liegen.");
    }

    @Test
    void testRestrictedAreaCreatedIfNoneExists() {
        // Prüfen, dass keine RestrictedArea existiert
        assertTrue(event.getRestrictedAreas().isEmpty());

        fight.step(event);

        assertFalse(event.getRestrictedAreas().isEmpty(),
                "Nach Step sollte RestrictedArea angelegt worden sein.");
    }

    @Test
    void testRestrictedAreaNotCreatedIfAlreadyExists() {
        RestrictedArea ra = new RestrictedArea(fightPos.x, fightPos.y, 8);
        event.addRestrictedArea(ra);
        event.grid.setObjectLocation(ra, fightPos);

        int before = event.getRestrictedAreas().size();

        fight.step(event);

        int after = event.getRestrictedAreas().size();

        assertEquals(before, after,
                "Keine zusätzliche RestrictedArea sollte erzeugt werden, wenn bereits eine existiert.");
    }

    @Test
    void testReleasePerimeterSecurityClearsTargetAndSetsEmergencyState() {
        Person perimeterSec = new Person(Person.PersonType.SECURITY);
        perimeterSec.setTargetPosition(new Int2D(1, 1));
        perimeterSec.setCurrentState(new states.RoamingState());
        event.agents.add(perimeterSec);

        // resolveFight direkt aufrufen → NICHT fight.setResolved(true)
        fight.resolveFight(event);

        assertNull(perimeterSec.getTargetPosition(), "Target sollte gecleart werden");
        assertInstanceOf(EmergencyState.class, perimeterSec.getCurrentState(), "State sollte EmergencyState sein");
    }


    @Test
    void testFightRemovedExactlyAtThirdTick() {
        fight.setResolved(true);

        // 3 steps → sollte entfernt sein
        fight.step(event);
        fight.step(event);
        fight.step(event);

        assertNull(event.grid.getObjectLocation(fight), "Fight sollte nach genau 3 Ticks entfernt sein");
    }


    static class SimpleMetricsCollector implements MetricsCollector {

        @Override
        public Map<String, List<Object>> getAllMetrics() {
            // Im Test nicht benötigt, daher leer
            return Collections.emptyMap();
        }

        @Override
        public void registerMetric(String name) {
            // Im Test nicht benötigt, daher leer
        }

        @Override
        @SuppressWarnings("java:S6213")
        public void recordMetric(String key, Object value) {
            // Im Test nicht implementiert, da keine Metrik-Aufzeichnung nötig
        }

        @Override
        public void recordZoneEntry(Agent agent, Zone zone) {
            // Im Test nicht benötigt, daher leer
        }

        @Override
        public void recordZoneExit(Agent agent, Zone zone) {
            // Im Test nicht benötigt, daher leer
        }

        @Override
        public void recordPanicEscape(Agent agent, Zone zone) {
            // Im Test nicht benötigt, daher leer
        }

        @Override
        public void recordEventTriggered(String eventType) {
            // Im Test nicht benötigt, daher leer
        }

        @Override
        public void recordTimeInZone(String zoneName, long ticks) {
            // Im Test nicht benötigt, daher leer
        }

        @Override
        public void recordQueueWait(String zoneName, long ticks) {
            // Im Test nicht benötigt, daher leer
        }
    }
}
