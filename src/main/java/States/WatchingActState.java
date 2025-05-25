package States;

import org.simulation.Agent;
import org.simulation.Event;
import org.simulation.Zone;
import sim.util.Int2D;

import java.util.List;

public class WatchingActState implements IStates {

    private int remainingTime = 50 + (int) (Math.random() * 20); // 50–70 Schritte
    private boolean initialized = false;

    @Override
    public IStates act(Agent agent, Event event) {
        Zone current = agent.getCurrentZone();
        Zone.ZoneType currentType = current != null ? current.getType() : null;
        if (!initialized) {
            agent.resetFlags();
            initialized = true;
        }


        Int2D pos = event.grid.getObjectLocation(agent);
        event.grid.setObjectLocation(agent, pos); // bleibt stehen

        System.out.println("Watching Act... Verbleibende Zeit: " + remainingTime);

        remainingTime--;

        if (remainingTime <= 0) {
            Zone zone = agent.getCurrentZone();
            if (zone != null) {
                zone.leave(agent);              // aus der Zone austragen
                agent.setCurrentZone(null);     // Referenz löschen
            }

            agent.setWatching(false);

            List<Zone.ZoneType> allOptions = List.of(Zone.ZoneType.FOOD, Zone.ZoneType.ACT_MAIN,
                    Zone.ZoneType.ACT_SIDE, Zone.ZoneType.EXIT);
            List<Zone.ZoneType> filtered = allOptions.stream()
                    .filter(z -> z != currentType)  // Nicht zur gleichen Zone zurück
                    .toList();
            if (!filtered.isEmpty()) {
                Zone.ZoneType nextType = filtered.get(event.random.nextInt(filtered.size()));
                Zone next = event.getZoneByType(nextType);
                if (next != null) {
                    agent.setTargetPosition(next.getPosition());
                    return new SeekingZoneState();
                }
            }
            return new RoamingState(); // zurück ins Schlendern
        }

        return this;
    }
}
