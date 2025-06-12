package States;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.simulation.Agent;
import org.simulation.Event;
import org.simulation.Zone;
import sim.util.Int2D;

import static org.junit.jupiter.api.Assertions.*;

public class PanicRunStateTest {

    private Event event;
    private Agent agent;
    private Zone exitZone;

    @BeforeEach
    public void setUp() {
        event = new Event(System.currentTimeMillis());
        event.start(); // 🔧 Grid & Agentenliste initialisieren

        agent = new Agent();
        agent.setEvent(event);
        event.agents.add(agent); // wichtig: damit Agent entfernt werden kann

        // Exit-Zone hinzufügen
        exitZone = new Zone(Zone.ZoneType.EMERGENCY_EXIT, new Int2D(5, 5), 10);
        event.zones.add(exitZone);

        // Agent in der Grid platzieren
        event.grid.setObjectLocation(agent, new Int2D(0, 0));
    }

    @Test
    public void testAgentEntersPanicStateAndMovesToExit() {
        agent.setCurrentState(new PanicRunState());

        for (int i = 0; i < 100; i++) {
            agent.step(event);
            if (!event.agents.contains(agent)) {
                break; // Agent wurde entfernt
            }
        }

        assertFalse(event.agents.contains(agent), "Agent sollte beim Erreichen des Exits entfernt werden");
    }

    @Test
    public void testPanicStateSetsCorrectFlags() {
        agent.setCurrentState(new PanicRunState());
        agent.step(event);

        assertTrue(agent.isPanicking(), "Agent sollte im Panic-Modus sein");
        assertNotNull(agent.getTargetPosition(), "Agent sollte ein Ziel (Exit) haben");
    }
}
