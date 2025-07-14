package states;

import org.simulation.Agent;
import org.simulation.Event;
import sim.util.Int2D;
import zones.Zone;

/**
 * Repräsentiert den Zustand eines Agenten, der das Event final verlässt
 * und sich gezielt zu einem EmergencyExit bewegt.
 *
 * @author Gerardo Carnevale
 */
public class ExitFinalizedState implements IStates {
    private boolean targetSet = false;
    private Int2D target;
    private Zone exitZone;

    @Override
    public IStates act(Agent agent, Event event) {
        Int2D pos = event.grid.getObjectLocation(agent);

        if (!targetSet) {
            exitZone = event.getNearestAvailableEmergencyExit(pos);
            if (exitZone != null) {
                target = exitZone.getPosition();
                agent.setTargetPosition(target);
                targetSet = true;
                System.out.println("ExitFinalizedState: Ziel gesetzt " + target);
            } else {
                return this; // bleibt stehen
            }
        }

        if (pos.equals(target)) {
            if (agent.tryEnterZone(exitZone)) {
                event.getCollector().recordPanicEscape(agent, exitZone);
                if (agent.getStopper() != null) agent.getStopper().stop();
                event.grid.remove(agent);
                event.agents.remove(agent);
                System.out.println("Agent hat den Exit erreicht und wurde entfernt.");
                return null;
            } else {
                return new QueueingState(agent, exitZone, this);
            }
        }

        // Bewegung zum Exit
        int dx = Integer.compare(target.x, pos.x);
        int dy = Integer.compare(target.y, pos.y);
        event.grid.setObjectLocation(agent, new Int2D(pos.x + dx, pos.y + dy));
        return this;
    }
}