package States;

import org.simulation.Agent;

public class EmergencyState implements IStates{
    // Dieser State ist nur für Nothelfer & Security - zum Unfallort gehen

    @Override
    public IStates act(Agent g) {
        return null;
    }
}
