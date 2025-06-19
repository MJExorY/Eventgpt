package org.simulation;


import org.junit.jupiter.api.Test;
import org.simulation.*;
import sim.util.Int2D;

import static org.junit.jupiter.api.Assertions.*;

public class FireDisturbanceTest {

    @Test
    public void testFireTriggersPanicInNearbyAgents() {
        Event event = new Event(System.currentTimeMillis(), 0, 0, 0);
        event.start();

        // Agent in der Nähe des Feuers
        Agent agent = new Agent();
        agent.setEvent(event);
        Int2D agentPos = new Int2D(10, 10);
        event.grid.setObjectLocation(agent, agentPos);
        event.agents.add(agent);

        // Feuer erzeugen in der Nähe
        FireDisturbance fire = new FireDisturbance(new Int2D(12, 12));
        fire.step(event);

        assertTrue(agent.isPanicking(), "Agent in Nähe sollte panisch sein");
        assertEquals("PanicRunState", agent.getCurrentState().getClass().getSimpleName());
    }

    @Test
    public void testFarAwayAgentDoesNotPanic() {
        Event event = new Event(System.currentTimeMillis(), 0, 0, 0);
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
}
