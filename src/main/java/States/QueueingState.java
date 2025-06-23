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
    private final int MAX_RETRIES = 5;
    private boolean initialized = false;

    public QueueingState(Agent agent, Zone targetZone, IStates followUpState) {
        this.targetZone = targetZone;
        this.followUpState = followUpState;
        this.waitingTime = 5 + new Random().nextInt(6); // 5–10 Schritte
        agent.setTargetPosition(targetZone.getPosition());

        // Für visuelle Darstellung: übernehme Flags aus dem Zielzustand
        if (followUpState instanceof WCState) {
            agent.setWC(true);
        }
        if (followUpState instanceof HungryThirstyState) {
            agent.setHungry(true);
        }
    }

    @Override
    public IStates act(Agent agent, Event event) {
        Int2D pos = event.grid.getObjectLocation(agent);
        event.grid.setObjectLocation(agent, pos); // Bleibt stehen

        if (!initialized) {
            agent.resetFlags();         // alles zurücksetzen …
            agent.setInQueue(true);     // … aber jetzt "Queue" setzen
            initialized = true;
        }

        // Hintereinanderreihung
        Int2D base = targetZone.getPosition();
        int offset = retryAttempts + 1; // pro Versuch einen Schritt weiter hinten
        Int2D queuePos = new Int2D(base.x, base.y + offset);
        event.grid.setObjectLocation(agent, queuePos);

        System.out.println("Agent wartet bei " + targetZone.getType() + " @ " + targetZone.getPosition()
                + " ... noch " + waitingTime + " Schritte. (Versuch " + retryAttempts + ")");

        waitingTime--;

        if (waitingTime <= 0) {
            if (!targetZone.isFull()) {
                boolean entered = agent.tryEnterZone(targetZone);
                if (entered) {
                    agent.setInQueue(false);
                    return followUpState; // Weiter im Zielzustand
                }
            }

            retryAttempts++;

            if (retryAttempts >= MAX_RETRIES) {
                System.out.println("Max. Versuche erreicht – Agent bricht Queue ab.");
                agent.setInQueue(false);
                return new RoamingState(); // gibt auf
            }

            // Zone weiterhin voll → neue kurze Wartezeit
            waitingTime = 3 + new Random().nextInt(4); // 3–6
            System.out.println("Zone immer noch voll – neue Wartezeit: " + waitingTime);
        }

        return this;
    }
}
