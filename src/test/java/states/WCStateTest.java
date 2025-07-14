package states;

import metrics.DefaultMetricsCollector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.simulation.Agent;
import org.simulation.Event;
import org.simulation.RestrictedArea;

import sim.util.Int2D;
import zones.Zone;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Testklasse für WCState, die das Verhalten eines Agenten im WCState überprüft.
 *
 * @author Dorothea Ziegler
 */
class WCStateTest {

    private Event event;
    private Agent agent;
    private Zone wcZone;
    private WCState state;

    @BeforeEach
    void setUp() {
        DefaultMetricsCollector collector = new DefaultMetricsCollector();
        for (Zone.ZoneType type : Zone.ZoneType.values()) {
            collector.registerMetric("ZoneEntry_" + type);
            collector.registerMetric("ZoneExit_" + type);
            collector.registerMetric("PanicEscape_" + type);
            collector.registerMetric("TimeInZone_" + type);
        }

        event = new Event(System.currentTimeMillis(), 100, 100, 1, collector);
        event.start();

        agent = new Agent();
        agent.setEvent(event);
        wcZone = new Zone(Zone.ZoneType.WC, new Int2D(20, 20), 5);
        event.zones.add(wcZone);

        event.grid.setObjectLocation(agent, new Int2D(0, 0));

        state = new WCState(event);
    }

    @Test
    void testInitializeMovesAgentTowardWC() {
        IStates result = state.act(agent, event);

        assertSame(state, result, "Sollte im WCState bleiben.");
        assertNotNull(agent.getTargetPosition(), "Agent sollte Zielposition gesetzt bekommen.");
    }

    @Test
    void testFallbackToRoamingIfNoWCZoneExists() {
        event.zones.clear();

        IStates result = state.act(agent, event);

        assertInstanceOf(RoamingState.class, result, "Sollte fallback zu RoamingState sein, wenn keine Zone existiert.");
    }

    @Test
    void testAgentMovesToWCZone() {
        state.act(agent, event);

        Int2D start = event.grid.getObjectLocation(agent);
        agent.setTargetPosition(wcZone.getPosition());
        IStates result = state.act(agent, event);

        Int2D newPos = event.grid.getObjectLocation(agent);
        assertNotEquals(start, newPos, "Agent sollte sich in Richtung Ziel bewegen.");
        assertSame(state, result);
    }

    @Test
    void testAgentEntersWCZone() {
        // Init einmal ausführen
        state.act(agent, event);

        // Agent direkt auf Zone setzen
        Int2D pos = wcZone.getPosition();
        event.grid.setObjectLocation(agent, pos);
        agent.setTargetPosition(pos);

        IStates result = state.act(agent, event);

        assertSame(state, result);
        assertFalse(agent.isInQueue(), "Agent sollte nicht mehr in Queue sein nach Eintritt.");
    }

    @Test
    void testAgentWaitsAndLeavesWCZone() throws Exception {
        // Agent direkt in Zone platzieren
        Int2D pos = wcZone.getPosition();
        agent.setCurrentZone(wcZone);
        agent.setTargetPosition(pos);
        event.grid.setObjectLocation(agent, pos);

        // Initial act() ausführen (setzt intern ggf. Variablen)
        state.act(agent, event);

        // Reflectively: enteredZone = true
        Field enteredZoneField = WCState.class.getDeclaredField("enteredZone");
        enteredZoneField.setAccessible(true);
        enteredZoneField.set(state, true);

        // waitTime auslesen
        Field waitTimeField = WCState.class.getDeclaredField("waitTime");
        waitTimeField.setAccessible(true);
        int waitTime = waitTimeField.getInt(state);

        // ticksInZone = waitTime - 1 setzen
        Field ticksInZoneField = WCState.class.getDeclaredField("ticksInZone");
        ticksInZoneField.setAccessible(true);
        ticksInZoneField.setInt(state, waitTime - 1);

        // Jetzt: 1 act() führt zu Wechsel
        IStates result = state.act(agent, event);

        assertInstanceOf(RoamingState.class, result, "Nach WAIT_TIME sollte Agent wieder RoamingState wechseln.");
        assertNull(agent.getCurrentZone(), "Agent sollte die Zone verlassen haben.");
        assertFalse(agent.isWC(), "Agent sollte WC-Flag zurücksetzen.");
    }


    @Test
    void testAgentTrappedInRestrictedArea() {
        // RestrictedArea direkt unter Agent
        Int2D pos = new Int2D(0, 0);
        RestrictedArea ra = new RestrictedArea(pos.x, pos.y, 1);
        ra.activate();
        event.addRestrictedArea(ra);
        event.grid.setObjectLocation(ra, pos);

        IStates result = state.act(agent, event);

        assertSame(state, result, "Agent sollte im WCState bleiben, wenn gefangen.");
    }
}
