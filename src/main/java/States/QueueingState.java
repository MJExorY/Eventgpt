package States;

import org.simulation.Agent;
import org.simulation.Event;
import org.simulation.Zone;
import sim.util.Int2D;

import java.util.Optional;
import java.util.Random;

public class QueueingState implements IStates {
    protected int waitingTime; // 5–15 Schritte
    private final Zone originalExitZone;
    private boolean initialized = false;

    public QueueingState(Agent agent, Zone exitZone) {
        this.originalExitZone = exitZone;
        this.waitingTime = 5 + (int) (Math.random() * 10);

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

        System.out.println("Agent wartet bei Exit-Zone (" + originalExitZone.getPosition() + ")... Noch " + waitingTime + " Schritte.");

        waitingTime--;

        if (waitingTime <= 0) {
            // Agent ist bereits drin → sofort WatchingActState
            if (!agent.isInQueue()) {
                return new WatchingMainActState();
            }

            if (!originalExitZone.isFull()) {
                boolean success = agent.tryEnterZone(originalExitZone);

                if (success) {
                    agent.setInQueue(false);
                    return new WatchingMainActState();
                }
            }

            Optional<Zone> alternative = event.zones.stream()
                    .filter(z -> z.getType() == Zone.ZoneType.EXIT && !z.equals(originalExitZone) && !z.isFull())
                    .findFirst();

            if (alternative.isPresent()) {
                Zone altZone = alternative.get();
                agent.setTargetPosition(altZone.getPosition());
                // return new SeekingZoneState();
            } else {
                // Keine Exit-Zone verfügbar → neue Wartezeit
                this.waitingTime = 3 + new Random().nextInt(5);
                System.out.println("Keine Exit-Zone verfügbar – Agent bleibt in FixedQueueingState mit neuer Wartezeit: " + this.waitingTime);
            }
        }

        return this;
    }
}
