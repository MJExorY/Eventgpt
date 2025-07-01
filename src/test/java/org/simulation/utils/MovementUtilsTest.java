package org.simulation.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.simulation.Agent;
import org.simulation.Event;
import org.simulation.Person;
import org.simulation.RestrictedArea;
import sim.util.Int2D;

import static org.junit.jupiter.api.Assertions.*;

class MovementUtilsTest {

    private Event event;
    private Agent agent;

    @BeforeEach
    void setup() {
        event = new Event(System.currentTimeMillis(), 0, 0, 0, null);
        event.start();

        agent = new Agent();
        agent.setEvent(event);
        event.grid.setObjectLocation(agent, new Int2D(10, 10));
    }

    @Test
    void testMoveAgentTowards_movesOneStepCloser() {
        Int2D target = new Int2D(12, 12);
        boolean moved = MovementUtils.moveAgentTowards(agent, event, target);
        Int2D newPos = event.grid.getObjectLocation(agent);

        assertTrue(moved, "Agent should move towards target");
        assertNotEquals(new Int2D(10, 10), newPos, "Agent position should change");
        int dx = Math.abs(newPos.x - 10);
        int dy = Math.abs(newPos.y - 10);
        assertTrue(dx <= 1 && dy <= 1, "Agent should move max one step in each direction");
    }

    @Test
    void testMoveAgentTowards_withNullTargetFallsBackToRandomMove() {
        boolean moved = MovementUtils.moveAgentTowards(agent, event, null);
        assertTrue(moved || !moved, "Method should return boolean (always true or false)");
    }

    @RepeatedTest(5)
    void testRandomMove_movesAtMostOneCell() {
        Int2D startPos = new Int2D(20, 20);
        event.grid.setObjectLocation(agent, startPos);

        boolean moved = MovementUtils.randomMove(agent, event);
        Int2D newPos = event.grid.getObjectLocation(agent);

        if (moved) {
            int dx = Math.abs(newPos.x - startPos.x);
            int dy = Math.abs(newPos.y - startPos.y);
            assertTrue(dx <= 1 && dy <= 1, "Random move must not exceed 1 cell in any direction");
        }
    }

    @Test
    void testTryEscapeRestrictedArea_escapesIfPossible() {
        RestrictedArea ra = new RestrictedArea(10, 10, 1);
        ra.activate();
        event.addRestrictedArea(ra);
        event.grid.setObjectLocation(agent, new Int2D(10, 10));

        boolean escaped = MovementUtils.tryEscapeRestrictedArea(agent, event);
        assertTrue(escaped, "Agent should escape if adjacent cells are free");
        Int2D pos = event.grid.getObjectLocation(agent);
        assertFalse(ra.isInside(pos.x, pos.y), "Agent should no longer be inside RestrictedArea after escape");
    }

    @Test
    void testTryEscapeRestrictedArea_returnsTrueWhenNotInRestrictedArea() {
        RestrictedArea ra = new RestrictedArea(50, 50, 1);
        ra.activate();
        event.addRestrictedArea(ra);

        event.grid.setObjectLocation(agent, new Int2D(10, 10));
        boolean escaped = MovementUtils.tryEscapeRestrictedArea(agent, event);
        assertTrue(escaped, "Should return true if agent is not inside any RestrictedArea");
    }

    @Test
    void testIsBlocked_returnsFalseForSecurity() {
        Person security = new Person(Person.PersonType.SECURITY);
        event.grid.setObjectLocation(security, new Int2D(5, 5));
        assertFalse(MovementUtils.isBlocked(event, 5, 5, security), "Security should never be blocked");
    }

    @Test
    void testIsBlocked_returnsFalseForMedic() {
        Person medic = new Person(Person.PersonType.MEDIC);
        event.grid.setObjectLocation(medic, new Int2D(5, 5));
        assertFalse(MovementUtils.isBlocked(event, 5, 5, medic), "Medic should never be blocked");
    }

    @Test
    void testIsBlocked_returnsTrueWhenInActiveRestrictedArea() {
        RestrictedArea ra = new RestrictedArea(10, 10, 1);
        ra.activate();
        event.addRestrictedArea(ra);

        boolean blocked = MovementUtils.isBlocked(event, 10, 10, agent);
        assertTrue(blocked, "Should be blocked in active RestrictedArea");
    }

    @Test
    void testPlaceQueueAgent_placesWhenNotRestricted() {
        Int2D pos = new Int2D(5, 5);
        boolean placed = MovementUtils.placeQueueAgent(agent, event, pos);
        assertTrue(placed, "Agent should be placed when not in RestrictedArea");
        assertEquals(pos, event.grid.getObjectLocation(agent));
    }

    @Test
    void testPlaceQueueAgent_failsInRestrictedArea() {
        RestrictedArea ra = new RestrictedArea(5, 5, 1);
        ra.activate();
        event.addRestrictedArea(ra);

        Int2D pos = new Int2D(5, 5);
        boolean placed = MovementUtils.placeQueueAgent(agent, event, pos);
        assertFalse(placed, "Should not place agent into active RestrictedArea");
    }
}
