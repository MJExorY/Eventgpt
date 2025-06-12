package States;

import org.simulation.Agent;
import org.simulation.Event;
import org.simulation.Zone;
import sim.util.Int2D;

public class HungryThirstyState implements IStates {

    private boolean initialized = false;
    private boolean enteredZone = false;
    private Int2D target;
    private int ticksInZone = 0;
    private final int WAIT_TIME = 20;

    @Override
    public IStates act(Agent agent, Event event) {
        Int2D currentPos = event.grid.getObjectLocation(agent);

        // 1. Initiales Ziel setzen
        if (!initialized) {
            agent.resetFlags();
            agent.setHungry(true);

            Zone foodZone = event.getZoneByType(Zone.ZoneType.FOOD);
            if (foodZone != null) {
                target = foodZone.getPosition();
                agent.setTargetPosition(target);
                initialized = true;
            } else {
                return new RoamingState(); // fallback
            }
        }

        // 2. Noch nicht in der Food-Zone?
        if (!enteredZone) {
            if (currentPos.equals(target)) {
                Zone zone = event.getZoneByPosition(target);
                if (zone != null) {
                    if (agent.tryEnterZone(zone)) {
                        enteredZone = true;
                    } else {
                        // Food-Zone ist VOLL → in Queue übergehen
                        return new QueueingState(agent, zone, this);
                    }
                } else {
                    // Position gehört zu keiner Zone
                    return new RoamingState();
                }
            } else {
                // Bewegung Richtung Ziel
                int dx = Integer.compare(target.x, currentPos.x);
                int dy = Integer.compare(target.y, currentPos.y);
                int newX = Math.max(0, Math.min(event.grid.getWidth() - 1, currentPos.x + dx));
                int newY = Math.max(0, Math.min(event.grid.getHeight() - 1, currentPos.y + dy));
                event.grid.setObjectLocation(agent, new Int2D(newX, newY));
            }
        } else {
            // 3. In der Zone → Wartezeit simulieren
            ticksInZone++;
            if (ticksInZone >= WAIT_TIME) {
                Zone zone = agent.getCurrentZone();
                if (zone != null) {
                    zone.leave(agent);
                    agent.setCurrentZone(null);
                }
                agent.setHungry(false);
                return new RoamingState();
            }
        }

        return this;
    }
}
