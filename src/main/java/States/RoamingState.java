package States;

import org.simulation.Agent;
import org.simulation.Event;
import org.simulation.Zone;
import sim.util.Int2D;

import java.util.List;

public class RoamingState implements IStates {

    @Override
    public IStates act(Agent agent, Event event) {
        agent.resetFlags();
        Int2D pos = event.grid.getObjectLocation(agent);

        // Bewegung
        int dx = event.random.nextInt(3) - 1;
        int dy = event.random.nextInt(3) - 1;
        int newX = Math.max(0, Math.min(event.grid.getWidth() - 1, pos.x + dx));
        int newY = Math.max(0, Math.min(event.grid.getHeight() - 1, pos.y + dy));
        event.grid.setObjectLocation(agent, new Int2D(newX, newY));

        // Zustandswechsel (zuerst prüfen!)
        if (event.random.nextDouble() < 0.005) {
            agent.setHungry(true);
            return new HungryThirstyState();
        }

        if (event.random.nextDouble() < 0.01) {
            agent.setWatching(true);
            return new WatchingActState();
        }

        if (event.random.nextDouble() < 0.003) {
            agent.setPanicking(true);
            return new PanicRunState();
        }

        if (event.random.nextDouble() < 0.005) {
            agent.setInQueue(true);
            return new QueueingState(agent, event.getZoneByType(Zone.ZoneType.EXIT));
        }

        // Neue Zone aufsuchen → nicht dieselbe wie zuletzt
        List<Zone.ZoneType> allOptions = List.of(
                Zone.ZoneType.FOOD,
                Zone.ZoneType.ACT_MAIN,
                Zone.ZoneType.ACT_SIDE,
                Zone.ZoneType.EXIT
        );

        // Letzte besuchte Zone ausschließen
        List<Zone.ZoneType> filtered = allOptions.stream()
                .filter(z -> z != agent.getLastVisitedZone())
                .toList();

        if (!filtered.isEmpty()) {
            Zone.ZoneType next = filtered.get(event.random.nextInt(filtered.size()));
            Zone targetZone = event.getZoneByType(next);
            if (targetZone != null) {
                agent.setTargetPosition(targetZone.getPosition());
                return new SeekingZoneState();
            }
        }

        return this;
    }

}
