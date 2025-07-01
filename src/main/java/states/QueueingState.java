package states;

import org.simulation.Agent;
import org.simulation.Event;
import sim.util.Int2D;
import zones.Zone;

import java.util.Random;

public class QueueingState implements IStates {
    private final Zone targetZone;
    private final IStates followUpState;
    private int waitingTime;
    private int retryAttempts = 0;
    private final int max_retries = 5;
    private boolean initialized = false;
    private int ticksInQueue = 0; // Timer der raufzaehlt wie lange Agent in queue ist
    private int geduld = 10; // Agent verlässt nach 10 Ticks die Queue
    private boolean comingFromPanic = false;
    private static final Random RANDOM = new Random();


    public QueueingState(Agent agent, Zone targetZone, IStates followUpState) {
        this.targetZone = targetZone;
        this.followUpState = followUpState;
        this.comingFromPanic = followUpState instanceof PanicRunState; // Abfang für PanicRunState
        //  Wartezeit & Geduld je nach Zone
        if (targetZone.getType() == Zone.ZoneType.FOOD) {
            waitingTime = 60 + RANDOM.nextInt(61);   // 60-120 Ticks
            geduld = 240;                                        // ~4 Min
        } else if (targetZone.getType() == Zone.ZoneType.WC) {
            waitingTime = 40 + RANDOM.nextInt(51);   // 40–90 Ticks
            geduld = 180;                              // ~3 min Geduld
        }
        // 1 Minute Geduld
        else {
            waitingTime = 5 + RANDOM.nextInt(6);    // 5–10 Ticks
            geduld = 10;                             // 10 Ticks
        } // 5–10 Schritte
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
            agent.setQueueStartTick(event.schedule.getSteps());   // Start-Tick merken
            initialized = true;
        }
        ticksInQueue++; // Timer zählt auf

        // Ungeduld implementiert
        if (!comingFromPanic && ticksInQueue > geduld) { // Wenn Ungeduld UND nicht aus Panic
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
                    long waitTicks = event.schedule.getSteps() - agent.getQueueStartTick();
                    agent.resetQueueStartTick();
                    event.getCollector()
                            .recordQueueWait(targetZone.getType().toString(), waitTicks);
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
