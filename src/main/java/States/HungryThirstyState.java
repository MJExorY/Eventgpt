package States;

import org.simulation.Agent;
import org.simulation.Event;
import org.simulation.Zone;

public class HungryThirstyState implements IStates {

    @Override
    public IStates act(Agent agent, Event event) {
        if (agent.isHungry()) {
            agent.resetFlags(); // nur zurücksetzen, wenn wir in einen neuen Zustand übergehen

            Zone foodZone = event.getZoneByType(Zone.ZoneType.FOOD);
            if (foodZone != null) {
                agent.setTargetPosition(foodZone.getPosition());
                return new SeekingZoneState();
            }
        }

        // Falls der Agent doch nicht hungrig ist, bleibt er im aktuellen Zustand
        return new RoamingState();
    }
}
