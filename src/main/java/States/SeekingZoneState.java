package States;

import org.simulation.Agent;
import org.simulation.Event;
import org.simulation.Zone;
import sim.util.Int2D;

public class SeekingZoneState implements IStates {

    private boolean initialized = false;

    @Override
    public IStates act(Agent agent, Event event) {
        if (!initialized) {
            agent.resetFlags();
            agent.setSeeking(true);
            initialized = true;
        }

        Int2D currentPos = event.grid.getObjectLocation(agent);
        Int2D targetPos = agent.getTargetPosition();

        // Sicherheits-Reset wenn Agent festhängt an Position (0,0)
        if (currentPos.x == 0 && currentPos.y == 0) {
            agent.setTargetPosition(null);
            agent.setSeeking(false);
            return new RoamingState();
        }

        // Ziel-Zone auslesen und vorab speichern
        Zone targetZone = event.getZoneByPosition(targetPos);
        agent.setCurrentZone(targetZone); // vorab setzen

        // Ziel erreicht?
        if (currentPos.equals(targetPos)) {
            agent.setSeeking(false);
            agent.setTargetPosition(null);

            if (targetZone != null && !targetZone.isFull()) {
                targetZone.enter(agent); // jetzt wirklich betreten
                agent.setCurrentZone(targetZone);
                agent.setLastVisitedZone(targetZone.getType());

                if (targetZone.getType() == Zone.ZoneType.FOOD) {
                    agent.setWatching(true); // jetzt in Zone → Farbe darf gesetzt werden
                    return new WatchingActState(); // essen
                } else if (targetZone.getType().name().startsWith("ACT")) {
                    agent.setWatching(true); // Bühne erreicht → jetzt blau werden
                    return new WatchingActState(); // Bühne genießen
                } else if (targetZone.getType() == Zone.ZoneType.EXIT) {
                    return new RoamingState(); // verlassen
                }
            } else {
                return new QueueingState(); // kein Platz
            }

            return new RoamingState(); // Fallback
        }

        // Bewegung Richtung Ziel
        int dx = Integer.compare(targetPos.x, currentPos.x);
        int dy = Integer.compare(targetPos.y, currentPos.y);

        int newX = Math.max(0, Math.min(event.grid.getWidth() - 1, currentPos.x + dx));
        int newY = Math.max(0, Math.min(event.grid.getHeight() - 1, currentPos.y + dy));
        event.grid.setObjectLocation(agent, new Int2D(newX, newY));

        return this;
    }

}
