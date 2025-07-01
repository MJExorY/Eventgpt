package states;

import org.simulation.Agent;
import org.simulation.Event;
import org.simulation.utils.MovementUtils;

public class RoamingState implements IStates {

    @Override
    public IStates act(Agent agent, Event event) {
        agent.resetFlags();
        agent.setRoaming(true);

        // Versuch aus RestrictedArea herauszukommen
        boolean escaped = MovementUtils.tryEscapeRestrictedArea(agent, event);
        if (!escaped) {
            // Agent bleibt stehen, falls gefangen
            return this;
        }

        // Zufällige Bewegung
        MovementUtils.randomMove(agent, event);

        // Zustandswechsel prüfen
        if (event.random.nextDouble() < 0.005) {
            agent.setHungry(true);
            return new HungryThirstyState(event);
        }

        if (event.random.nextDouble() < 0.003) {
            agent.setWC(true);
            return new WCState(event);
        }

        if (event.random.nextDouble() < 0.015) {
            agent.setWatchingMain(true);
            return new WatchingMainActState(event);
        }

        if (event.random.nextDouble() < 0.011) {
            agent.setWatchingSide(true);
            return new WatchingSideActState(event);
        }

        return this;
    }
}
