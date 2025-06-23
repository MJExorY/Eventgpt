package States;

import org.simulation.Agent;
import org.simulation.Event;
import org.simulation.Zone;
import sim.util.Int2D;

import java.util.Random;

public class QueueingState implements IStates {
    private final Zone targetZone;
    private final IStates followUpState;
    private int waitingTime;
    private int retryAttempts = 0;
    private int max_retries = 5;
    private boolean initialized = false;

    private int ticksInQueue = 0; // Timer der raufzählt wie lange Agent in queue ist
    private int geduld = 10; // Agent verlässt nach 10 Ticks die Queue

    public QueueingState(Agent agent, Zone targetZone, IStates followUpState) {
        this.targetZone = targetZone;
        this.followUpState = followUpState;
        this.waitingTime = 5 + new Random().nextInt(6); // 5–10 Schritte
        agent.setTargetPosition(targetZone.getPosition());

        // übernehme Flags aus dem Zielzustand
        if (followUpState instanceof WCState) agent.setWC(true);
        if (followUpState instanceof HungryThirstyState) agent.setHungry(true);
    }

    @Override
    public IStates act(Agent agent, Event event) {
        if (!initialized) {
            agent.resetFlags();
            agent.setInQueue(true);
            initialized = true;
        }
        ticksInQueue++; // Timer zählt auf

        // Ungeduld implementiert
        if (ticksInQueue > geduld) {
            System.out.println("Agent verlässt Queue aufgrund Ungeduld = wechselt zu Roaming");
            agent.setInQueue(false);
            return new RoamingState();
        }

        // Hintereinanderreihung
        Int2D base = targetZone.getPosition();
        int offset = retryAttempts + 1; // Der Nächste stellt sich dahinter
        Int2D queuePos = new Int2D(base.x, base.y + offset);
        event.grid.setObjectLocation(agent, queuePos);

        System.out.println("Agent wartet bei " + targetZone.getType() + " @ " + base
                + " ... noch " + waitingTime + " Schritte. (Versuch " + retryAttempts + ")");

        waitingTime--;

        if (waitingTime <= 0) {
            if (!targetZone.isFull()) {
                boolean entered = agent.tryEnterZone(targetZone);
                if (entered) {
                    agent.setInQueue(false);
                    return followUpState;
                }
            }

            retryAttempts++;

            if (retryAttempts >= max_retries) {
                System.out.println("Max. Versuche erreicht – Agent bricht Queue ab.");
                agent.setInQueue(false);
                return new RoamingState();
            }

            waitingTime = 3 + new Random().nextInt(4); // neue Wartezeit
            System.out.println("Zone weiterhin voll – neue Wartezeit: " + waitingTime);
        }

        return this;
    }
}