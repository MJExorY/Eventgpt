package States;

import org.simulation.Agent;
import org.simulation.Event;

public interface IStates {
    IStates act(Agent g, Event event);

}
