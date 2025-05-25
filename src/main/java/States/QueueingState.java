package States;

import org.simulation.Agent;
import org.simulation.Event;
import sim.util.Int2D;

public class QueueingState implements IStates {
    protected int waitingTime = 5 + (int) (Math.random() * 10); // 5–15 Schritte

    private boolean initialized = false;

    @Override
    public IStates act(Agent agent, Event event) {
        if (!initialized) {
            agent.resetFlags(); // andere Zustände deaktivieren
            agent.setInQueue(true);
            initialized = true;
        }

        Int2D pos = event.grid.getObjectLocation(agent);
        event.grid.setObjectLocation(agent, pos); // bleibt stehen

        System.out.println("Wartet in der Schlange... " + waitingTime);

        waitingTime--;

        if (waitingTime <= 0) {
            agent.setInQueue(false); // verlässt die Warteschlange
            return new WatchingActState(); // „Act beginnt“
        }

        return this;
    }
}
