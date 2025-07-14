package states;

import org.simulation.Agent;
import org.simulation.Event;
import zones.Zone;
import sim.util.Int2D;

import java.util.Random;

/**
 * Repräsentiert einen Zustand, in dem ein Agent in einer Warteschlange (Queue) steht.
 * Der Agent wartet eine bestimmte Anzahl an Ticks, um dann zu prüfen, ob er die Zielzone betreten kann.
 * Falls die Wartezeit oder Geduld überschritten wird oder der Eintritt fehlschlägt, wechselt er ggf. in einen anderen Zustand.
 *
 * @author cb-235866
 */
public class QueueingState implements IStates {
    private final Zone targetZone;
    private final IStates followUpState;
    private int waitingTime;
    private int retryAttempts = 0;
    private boolean initialized = false;
    private int ticksInQueue = 0;
    private final int geduld = 10;
    private boolean comingFromPanic = false;

    public QueueingState(Agent agent, Zone targetZone, IStates followUpState) {
        this.targetZone = targetZone;
        this.followUpState = followUpState;
        this.comingFromPanic = followUpState instanceof ExitFinalizedState;
        this.waitingTime = 5 + new Random().nextInt(6);
        agent.setTargetPosition(targetZone.getPosition());
    }

    @Override
    public IStates act(Agent agent, Event event) {
        if (!initialized) {
            agent.resetFlags();
            agent.setInQueue(true);
            initialized = true;
        }

        ticksInQueue++;
        if (!comingFromPanic && ticksInQueue > geduld) {
            agent.setInQueue(false);
            return new RoamingState();
        }

        // Queue-Stellen verteilen
        Int2D base = targetZone.getPosition();
        Int2D queuePos = new Int2D(base.x, base.y + retryAttempts + 1);
        event.grid.setObjectLocation(agent, queuePos);

        waitingTime--;
        if (waitingTime <= 0) {
            if (!targetZone.isFull() && agent.tryEnterZone(targetZone)) {
                agent.setInQueue(false);
                return followUpState;
            }
            retryAttempts++;
            waitingTime = 3 + new Random().nextInt(4);
        }

        return this;
    }
}