package states;

import org.simulation.Agent;
import zones.EmergencyRouteLinks;
import zones.EmergencyRouteRechts;
import zones.EmergencyRouteStraight;
import org.simulation.Event;
import zones.Zone;
import org.simulation.utils.MovementUtils;
import sim.util.Int2D;
import sim.util.Bag;

/**
 * Der PanicRunState repräsentiert den Zustand eines Agenten während einer Panikreaktion.
 * In diesem Zustand versucht der Agent zunächst eine Notausgangsroute zu erreichen und danach
 * über einen regulären oder Notausgang die Simulation zu verlassen.
 * Panikticks werden gezählt und bei erfolgreicher Flucht registriert.
 *
 * @author cb-235866
 */
public class PanicRunState implements IStates {
    private boolean reachedEmergencyRoute = false;
    private Int2D target;

    @Override
    public IStates act(Agent agent, Event event) {
        Int2D currentPos = event.grid.getObjectLocation(agent);

        if (!agent.isPanicking()) {
            agent.resetFlags();
            agent.setPanicking(true);
        }

        if (!reachedEmergencyRoute) {
            if (target == null) {
                target = findNearestEmergencyRoute(event, currentPos);
                if (target != null) {
                    agent.setTargetPosition(target);
                } else {
                    return new RoamingState();
                }
            }

            if (currentPos.equals(target)) {
                reachedEmergencyRoute = true;
                return new ExitFinalizedState();
            }
        }

        // → Movement auslagern:
        boolean escaped = MovementUtils.tryEscapeRestrictedArea(agent, event);
        if (!escaped) {
            return this; // Agent bleibt stehen
        }

        MovementUtils.moveAgentTowards(agent, event, target);

        return this;
    }

    private Int2D findNearestEmergencyRoute(Event event, Int2D currentPos) {
        Int2D nearest = null;
        int minDist = Integer.MAX_VALUE;

        Bag all = event.grid.getAllObjects();
        for (Object o : all) {
            Int2D pos = null;

            if (o instanceof EmergencyRouteRechts er) {
                pos = er.getPosition();
            } else if (o instanceof EmergencyRouteLinks) {
                pos = new Int2D(20, 50); // HARDCODED
            } else if (o instanceof EmergencyRouteStraight) {
                pos = new Int2D(50, 20); // HARDCODED
            }

            if (pos != null) {
                int dist = Math.abs(pos.x - currentPos.x) + Math.abs(pos.y - currentPos.y);
                if (dist < minDist) {
                    minDist = dist;
                    nearest = pos;
                }
            }
        }

        return nearest;
    }
} 