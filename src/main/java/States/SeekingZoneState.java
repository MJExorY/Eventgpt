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

        // Ziel erreicht?
        if (currentPos.equals(targetPos)) {
            Zone targetZone = event.getZoneByPosition(targetPos);
            agent.setSeeking(false);
            agent.setTargetPosition(null);

            if (targetZone != null) {
                boolean entered = agent.tryEnterZone(targetZone); // zentrale Prüfung

                if (entered) {
                    return switch (targetZone.getType()) {
                        case FOOD -> {
                            agent.setWatching(true); // jetzt in Zone → Farbe darf gesetzt werden
                            yield new WatchingActState(); // jetzt in Zone → Farbe darf gesetzt werden
                        }
                        case WC -> {
                            agent.setWatching(true);
                            yield new WCState();
                        }
                        case ACT_MAIN -> {
                            agent.setWatching(true); // Bühne erreicht → jetzt blau werden
                            yield new WatchingActState(); // Bühne erreicht → jetzt blau werden
                        }
                        case ACT_SIDE -> {
                            agent.setWatching(true); // Bühne erreicht → jetzt blau werden
                            yield new WatchingActState(); // Bühne erreicht → jetzt blau werden
                        }
                        case EXIT -> new RoamingState(); // verlassen
                        case EMERGENCY_EXIT -> new RoamingState();
                    };
                } else {
                    return new QueueingState(agent, event.getZoneByType(Zone.ZoneType.EXIT)); // falls .tryEnterZone false zurückgibt
                }
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
