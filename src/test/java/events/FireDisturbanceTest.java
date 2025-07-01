package events;


import org.junit.jupiter.api.Test;
import org.simulation.Agent;
import org.simulation.Event;
import org.simulation.Person;
import org.simulation.RestrictedArea;
import sim.util.Int2D;
import states.EmergencyState;
import states.IStates;

import static org.junit.jupiter.api.Assertions.*;

public class FireDisturbanceTest {

    @Test
    void testFireTriggersPanicInNearbyAgents() {
        Event event = new Event(System.currentTimeMillis(), 0, 0, 0, null);
        event.start();

        // Besucher in der Nähe des Feuers
        Person visitor = new Person(Person.PersonType.VISITOR);
        visitor.setEvent(event);
        Int2D agentPos = new Int2D(10, 10);
        event.grid.setObjectLocation(visitor, agentPos);
        event.agents.add(visitor);

        // Feuer erzeugen in der Nähe
        FireDisturbance fire = new FireDisturbance(new Int2D(12, 12));
        fire.step(event);

        assertTrue(visitor.isPanicking(), "Agent in Nähe sollte panisch sein");
        assertEquals("PanicRunState", visitor.getCurrentState().getClass().getSimpleName());
    }

    @Test
    void testFarAwayAgentDoesNotPanic() {
        Event event = new Event(System.currentTimeMillis(), 0, 0, 0, null);
        event.start();

        Agent agent = new Agent();
        agent.setEvent(event);
        Int2D agentPos = new Int2D(50, 50);
        event.grid.setObjectLocation(agent, agentPos);
        event.agents.add(agent);

        FireDisturbance fire = new FireDisturbance(new Int2D(10, 10));
        fire.step(event);

        assertFalse(agent.isPanicking(), "Agent außerhalb des Radius sollte nicht panisch sein");
    }

    @Test
    void testRestrictedAreaCreated() {
        Event event = new Event(System.currentTimeMillis(), 0, 0, 0, null);
        event.start();

        FireDisturbance fire = new FireDisturbance(new Int2D(10, 10));
        event.grid.setObjectLocation(fire, fire.getPosition());

        fire.step(event);

        assertFalse(event.getRestrictedAreas().isEmpty(),
                "Nach step() sollte eine RestrictedArea erstellt sein.");
        RestrictedArea ra = event.getRestrictedAreas().get(0);
        assertEquals(10, ra.getCenterX());
        assertEquals(10, ra.getCenterY());
    }

    @Test
    void testSecurityGetsPositionedAroundFire() {
        Event event = new Event(System.currentTimeMillis(), 0, 0, 0, null);
        event.start();

        // Security-Agent erzeugen
        Person security = new Person(Person.PersonType.SECURITY);
        security.setEvent(event);
        event.agents.add(security);
        event.grid.setObjectLocation(security, new Int2D(5, 5));

        FireDisturbance fire = new FireDisturbance(new Int2D(10, 10));
        event.grid.setObjectLocation(fire, fire.getPosition());

        fire.step(event);

        assertNotNull(security.getTargetPosition(),
                "Security sollte eine neue Zielposition erhalten.");
    }

    @Test
    void testResolveDeactivatesRestrictedArea() {
        Event event = new Event(System.currentTimeMillis(), 0, 0, 0, null);
        event.start();

        // RestrictedArea vorher anlegen
        RestrictedArea ra = new RestrictedArea(10, 10, 5);
        event.addRestrictedArea(ra);

        FireDisturbance fire = new FireDisturbance(new Int2D(10, 10));
        event.grid.setObjectLocation(fire, fire.getPosition());

        // Security-Agent anlegen (damit resolve Security freigibt)
        Person sec = new Person(Person.PersonType.SECURITY);
        sec.setCurrentState((IStates) new EmergencyState());
        sec.setEvent(event);
        event.agents.add(sec);

        fire.resolve(event);

        assertFalse(ra.isActive(), "RestrictedArea sollte deaktiviert sein nach resolve()");
    }

    @Test
    void testResolveRemovesFireDisturbance() {
        Event event = new Event(System.currentTimeMillis(), 0, 0, 0, null);
        event.start();

        FireDisturbance fire = new FireDisturbance(new Int2D(10, 10));
        event.grid.setObjectLocation(fire, fire.getPosition());

        fire.resolve(event);

        assertNull(event.grid.getObjectLocation(fire),
                "FireDisturbance sollte nach resolve() aus dem Grid entfernt werden.");
    }


}
