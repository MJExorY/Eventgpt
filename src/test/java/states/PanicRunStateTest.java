package states;

import metrics.DefaultMetricsCollector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.simulation.Agent;
import org.simulation.Event;
import zones.Zone;
import sim.util.Int2D;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testklasse für PanicRunState, die das Verhalten eines Agenten in Panik überprüft.
 *
 * @author cb-235866
 */
public class PanicRunStateTest {

    private Event event;
    private Agent agent;

    @BeforeEach
    public void setUp() {
        DefaultMetricsCollector collector = new DefaultMetricsCollector();
        for (Zone.ZoneType type : Zone.ZoneType.values()) {
            collector.registerMetric("ZoneEntry_" + type);
            collector.registerMetric("ZoneExit_" + type);
            collector.registerMetric("PanicEscape_" + type);
        }

        event = new Event(System.currentTimeMillis(), 0, 0, 0, collector);

        event.start();

        agent = new Agent();
        agent.setEvent(event);
        event.agents.add(agent); // wichtig: damit Agent entfernt werden kann

        // Exit-Zone hinzufügen
        Zone exitZone = new Zone(Zone.ZoneType.EMERGENCY_EXIT, new Int2D(5, 5), 10);
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
