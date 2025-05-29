package States;

import org.junit.jupiter.api.Test;
import org.simulation.Agent;
import org.simulation.Event;
import org.simulation.Zone;
import sim.field.grid.SparseGrid2D;
import sim.util.Int2D;

import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class QueueingStateTest {

    @Test
    public void testFirstActInitializesQueueing() {
        // Arrange
        Agent agent = new Agent();
        DummyEvent event = new DummyEvent(10, 10);
        Int2D position = new Int2D(3, 3);
        event.grid.setObjectLocation(agent, position);

        Zone exitZone = new Zone(Zone.ZoneType.EXIT, new Int2D(3, 4), 1);
        event.zones.add(exitZone);

        FixedQueueingState state = new FixedQueueingState(agent, exitZone, 3);

        // Act
        IStates nextState = state.act(agent, event);

        // Assert
        assertTrue(agent.isInQueue(), "Agent sollte in der Warteschlange sein");
        assertEquals(position, event.grid.getObjectLocation(agent),
                "Agent sollte sich nicht bewegen");
        assertSame(state, nextState, "Zustand sollte beim ersten Aufruf gleich bleiben");
    }

    @Test
    public void testWaitsUntilTimeExpires() {
        Agent agent = new Agent();
        DummyEvent event = new DummyEvent(10, 10);
        Int2D position = new Int2D(5, 5);
        event.grid.setObjectLocation(agent, position);

        Zone exitZone = new Zone(Zone.ZoneType.EXIT, new Int2D(5, 6), 1);
        event.zones.add(exitZone);

        Agent occupyingAgent = new Agent();
        exitZone.enter(occupyingAgent);

        // Setze waitingTime auf 3, sodass wir steuern können
        FixedQueueingState state = new FixedQueueingState(agent, exitZone, 3);

        // Schritt 1–2: countdown
        state.act(agent, event); // waitingTime = 3 → 2
        state.act(agent, event); // waitingTime = 2 → 1

        exitZone.leave(occupyingAgent); // 🟢 Zone freigeben vor dem letzten Tick

        state.act(agent, event); // waitingTime = 1 → 0 (Agent darf nun rein)

        IStates result = state.act(agent, event); // tatsächlicher Eintritt

        assertFalse(agent.isInQueue(), "Agent sollte Warteschlange verlassen haben");
        assertInstanceOf(WatchingActState.class, result, "Agent sollte WatchingActState betreten");
    }




    @Test
    public void testAgentWaitsWhenZoneIsFull() {
        Agent agent = new Agent();
        DummyEvent event = new DummyEvent(10, 10);
        Zone exitZone = new Zone(Zone.ZoneType.EXIT, new Int2D(2, 2), 1);
        event.zones.add(exitZone);
        exitZone.enter(new Agent());

        FixedQueueingState state = new FixedQueueingState(agent, exitZone, 1);
        state.act(agent, event); // t = 0

        IStates nextState = state.act(agent, event); // wartezeit abgelaufen, aber Zone voll

        assertTrue(agent.isInQueue());
        assertSame(state, nextState);
    }
    @Test
    public void testAgentFindsAlternativeExitZone() {
        Agent agent = new Agent();
        DummyEvent event = new DummyEvent(10, 10);

        // Full zone
        Zone fullExit = new Zone(Zone.ZoneType.EXIT, new Int2D(2, 2), 1);
        Agent blocker = new Agent();
        fullExit.enter(blocker);

        // Free zone
        Zone freeExit = new Zone(Zone.ZoneType.EXIT, new Int2D(3, 3), 1);

        event.zones.add(fullExit);
        event.zones.add(freeExit);

        FixedQueueingState state = new FixedQueueingState(agent, fullExit, 1);
        state.act(agent, event); // t = 1 → sollte auf 0 sinken

        IStates nextState = state.act(agent, event); // t = 0

        assertInstanceOf(SeekingZoneState.class, nextState, "Sollte in alternative Exit-Zone wechseln");
        assertEquals(freeExit.getPosition(), agent.getTargetPosition(), "Agent sollte alternative Zone ansteuern");
    }
    @Test
    public void testAgentExitsAfterWaitIfSpaceAvailable() {
        Agent agent = new Agent();
        DummyEvent event = new DummyEvent(10, 10);

        Zone exitZone = new Zone(Zone.ZoneType.EXIT, new Int2D(4, 4), 1);
        event.zones.add(exitZone);

        FixedQueueingState state = new FixedQueueingState(agent, exitZone, 1);
        state.act(agent, event); // t = 0
        IStates nextState = state.act(agent, event); // t = -1
        assertFalse(agent.isInQueue());
        assertInstanceOf(WatchingActState.class, nextState);
    }

    @Test
    public void testAgentWaitsAgainWhenNoExitAvailable() {
        Agent agent = new Agent();
        DummyEvent event = new DummyEvent(10, 10);

        Zone fullExit = new Zone(Zone.ZoneType.EXIT, new Int2D(1, 1), 1);
        fullExit.enter(new Agent()); // besetze die Zone
        event.zones.add(fullExit);

        FixedQueueingState state = new FixedQueueingState(agent, fullExit, 0);
        IStates nextState = state.act(agent, event);

        assertTrue(agent.isInQueue());
        assertTrue(nextState instanceof FixedQueueingState);

        FixedQueueingState castedState = (FixedQueueingState) nextState;
        int newWaitingTime = castedState.getWaitingTime();
        assertTrue(newWaitingTime >= 3 && newWaitingTime <= 7,
                "WaitingTime sollte zwischen 3 und 7 liegen, ist aber: " + newWaitingTime);
    }




    // ===== Dummy-Hilfsklassen =====

    public static class DummyEvent extends Event {
        public DummyEvent(int width, int height) {
            super(System.currentTimeMillis());
            this.grid = new SparseGrid2D(width, height);
        }
    }

    public static class FixedQueueingState extends QueueingState {
        private final Zone originalExitZone;
        private boolean initialized = false;
        public FixedQueueingState(Agent agent, Zone zone, int fixedTime) {
            super(agent, zone);
            this.waitingTime = fixedTime;
            originalExitZone = zone;
        }

        public int getWaitingTime() {
            return this.waitingTime;
        }

        @Override
        public IStates act(Agent agent, Event event) {
            if (!initialized) {
                agent.resetFlags();
                agent.setInQueue(true);
                initialized = true;
            }

            Int2D pos = event.grid.getObjectLocation(agent);
            event.grid.setObjectLocation(agent, pos);

            System.out.println("FixedQueueingState wartet... Noch " + waitingTime + " Schritte.");
            waitingTime--;

            if (waitingTime <= 0) {
                // Agent ist bereits drin → sofort WatchingActState
                if (!agent.isInQueue()) {
                    return new WatchingActState();
                }

                if (!originalExitZone.isFull()) {
                    boolean success = agent.tryEnterZone(originalExitZone);

                    if (success) {
                        agent.setInQueue(false);
                        return new WatchingActState();
                    }
                }

                Optional<Zone> alternative = event.zones.stream()
                        .filter(z -> z.getType() == Zone.ZoneType.EXIT && !z.equals(originalExitZone) && !z.isFull())
                        .findFirst();

                if (alternative.isPresent()) {
                    Zone altZone = alternative.get();
                    agent.setTargetPosition(altZone.getPosition());
                    return new SeekingZoneState();
                } else {
                    // Keine Exit-Zone verfügbar → neue Wartezeit
                    this.waitingTime = 3 + new Random().nextInt(5);
                    System.out.println("Keine Exit-Zone verfügbar – Agent bleibt in FixedQueueingState mit neuer Wartezeit: " + this.waitingTime);
                }
            }

            return this;
        }





    }



}
