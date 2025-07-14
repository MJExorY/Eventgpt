package events;

import org.simulation.Agent;
import org.simulation.Event;
import states.PanicRunState;
import metrics.MetricsCollector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import zones.Zone;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testklasse für StormDisturbance.
 * Prüft Sicherheitsverhalten, Notfallreaktionen, Panikausbreitung und Grid-Interaktion.
 *
 * @author Kevin Jan Seibold
 */
class StormDisturbanceTest {

    private Event event;
    private StormDisturbance storm;

    @BeforeEach
    void setUp() {
        event = new Event(System.currentTimeMillis(), 20, 20, 0, new DummyMetricsCollector());
        event.start();
        event.agents.clear();

        storm = new StormDisturbance();
    }

    @Test
    void testStormTriggersPanic() {
        Agent a1 = new Agent();
        Agent a2 = new Agent();
        event.agents.add(a1);
        event.agents.add(a2);

        storm.step(event);

        assertTrue(a1.isPanicking(), "Agent 1 sollte in Panik geraten");
        assertInstanceOf(PanicRunState.class, a1.getCurrentState(), "Agent 1 sollte PanicRunState haben");

        assertTrue(a2.isPanicking(), "Agent 2 sollte in Panik geraten");
        assertInstanceOf(PanicRunState.class, a2.getCurrentState(), "Agent 2 sollte PanicRunState haben");
    }

    @Test
    void testAlarmOnlyTriggersOnce() {
        // Erster Schritt: Alarm soll ausgelöst werden
        storm.step(event);
        assertTrue(event.isStormAlertTriggered(), "Alarm sollte beim ersten Schritt ausgelöst werden");

        // Zweiter Schritt: Panischer Agent darf Alarm nicht erneut auslösen
        Agent panickingAgent = new Agent();
        panickingAgent.setPanicking(true);
        panickingAgent.setCurrentState(new PanicRunState());

        event.agents.clear();
        event.agents.add(panickingAgent);

        storm.step(event);

        // Alarm bleibt weiterhin aktiv
        assertTrue(event.isStormAlertTriggered(), "Alarm sollte nach erneutem Schritt weiterhin als ausgelöst markiert sein");
    }

    @Test
    void testAlreadyPanickingAgentIsNotChanged() {
        Agent agent = new Agent();
        agent.setPanicking(true);
        PanicRunState panicState = new PanicRunState();
        agent.setCurrentState(panicState);

        event.agents.add(agent);

        storm.step(event);

        assertSame(panicState, agent.getCurrentState(), "Bereits panischer Agent sollte gleichen Zustand behalten");
    }

    static class DummyMetricsCollector implements MetricsCollector {
        @Override
        public Map<String, List<Object>> getAllMetrics() {
            return Collections.emptyMap(); // Für Unit-Tests nicht erforderlich
        }

        @Override
        public void registerMetric(String name) {
            // Keine Metriken nötig im Testkontext
        }

        @Override
        @SuppressWarnings("java:S6213")
        public void recordMetric(String key, Object value) {
            // Metriken werden im Test nicht aufgezeichnet
        }

        @Override
        public void recordZoneEntry(Agent agent, Zone zone) {
            // Kein Verhalten nötig für Zoneneintritte in Tests
        }

        @Override
        public void recordZoneExit(Agent agent, Zone zone) {
            // Kein Verhalten nötig für Zonenaustritte in Tests
        }

        @Override
        public void recordPanicEscape(Agent agent, Zone zone) {
            // Diese Methode ist für diese Tests irrelevant
        }

        @Override
        public void recordEventTriggered(String eventType) {
            // Event-Trigger müssen in diesen Tests nicht verfolgt werden
        }

        @Override
        public void recordTimeInZone(String zoneName, long ticks) {
            // In Unit-Tests nicht benötigt – Simulation der Zeit ist nicht Teil des Tests
        }

        @Override
        public void recordQueueWait(String zoneName, long ticks) {
            // Wartezeitaufzeichnungen sind für die Tests nicht relevant
        }
    }
}
