package States;

import org.simulation.Agent;
import org.simulation.Event;
import org.simulation.Zone;
import sim.util.Int2D;

public class WCState implements IStates {

    private boolean initialized = false;
    private boolean enteredZone = false;
    private Int2D target;
    private int ticksInZone = 0;
    private final int WAIT_TIME = 10;

    @Override
    public IStates act(Agent agent, Event event) {
        Int2D currentPos = event.grid.getObjectLocation(agent);

        if (!initialized) {
            agent.resetFlags();
            agent.setWC(true);

            Zone wcZone = event.getZoneByType(Zone.ZoneType.WC);
            if (wcZone != null) {
                target = wcZone.getPosition();
                agent.setTargetPosition(target);
                initialized = true;
            } else {
                return new RoamingState(); // fallback
            }
        }

        if (!enteredZone) {
            if (currentPos.equals(target)) {
                Zone zone = event.getZoneByPosition(target);
                if (zone != null && agent.tryEnterZone(zone)) {
                    enteredZone = true;
                } else {
                    return new RoamingState(); // fallback
                }
            } else {
                // Schrittweise Bewegung in Richtung Ziel
                int dx = Integer.compare(target.x, currentPos.x);
                int dy = Integer.compare(target.y, currentPos.y);
                int newX = Math.max(0, Math.min(event.grid.getWidth() - 1, currentPos.x + dx));
                int newY = Math.max(0, Math.min(event.grid.getHeight() - 1, currentPos.y + dy));
                event.grid.setObjectLocation(agent, new Int2D(newX, newY));
            }
        } else {
            // Ist in der WC-Zone - Time
            ticksInZone++;
            if (ticksInZone >= WAIT_TIME) {
                Zone zone = agent.getCurrentZone();
                if (zone != null) {
                    zone.leave(agent);
                    agent.setCurrentZone(null);
                }
                return new RoamingState();
            }
        }

        return this;
    }
}
