package States;

import org.simulation.Agent;
import org.simulation.Event;
import org.simulation.Zone;
import sim.util.Int2D;

public class WCState implements IStates {
    private int remainingTime = 5 + (int) (Math.random() * 5); // Nur 5-10 Schritte
    private boolean initialized = false;

    @Override
    public IStates act(Agent agent, Event event) {
        if (!initialized) {
            agent.resetFlags();
            agent.setWatching(true); // Damit sie orange bleiben
            initialized = true;
        }

        Int2D pos = event.grid.getObjectLocation(agent);
        event.grid.setObjectLocation(agent, pos); // bleibt stehen

        //System.out.println("WC-Besuch... Verbleibende Zeit: " + remainingTime);

        remainingTime--;

        if (remainingTime <= 0) {
            Zone zone = agent.getCurrentZone();
            if (zone != null) {
                zone.leave(agent);
                agent.setCurrentZone(null);
            }
            agent.setWatching(false);

            // Direkt zurück zum Roaming
            return new RoamingState();
        }

        return this;
    }
}