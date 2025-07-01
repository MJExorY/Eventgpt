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

public class PanicRunState implements IStates {
    private boolean reachedEmergencyRoute = false;
    private Int2D target;
    private Zone exitZone;
    private int ticksInPanic = 0;

    @Override
    public IStates act(Agent agent, Event event) {
        Int2D currentPos = event.grid.getObjectLocation(agent);

        if (!agent.isPanicking()) {
            agent.resetFlags();
            agent.setPanicking(true);
        }

        ticksInPanic++;
        // PHASE 1 – Ziel: EmergencyRoute
        if (!reachedEmergencyRoute) {
            if (target == null) {
                target = findNearestEmergencyRoute(event, currentPos);
                if (target != null) {
                    agent.setTargetPosition(target);
                    System.out.println("Ziel gesetzt: EmergencyRoute bei " + target);
                } else {
                    System.out.println("Keine Emergency Route gefunden – zurück zu Roaming");
                    return new RoamingState();
                }
            }

            if (currentPos.equals(target)) {
                reachedEmergencyRoute = true;
                System.out.println("EmergencyRoute erreicht – wechsle zu Exit");

                exitZone = event.getNearestAvailableExit(currentPos);
                if (exitZone != null) {
                    target = exitZone.getPosition();
                    agent.setTargetPosition(target);
                    System.out.println("Neuer Zielpunkt: Exit bei " + target);
                } else {
                    System.out.println("⚠ Kein Exit verfügbar – Agent bleibt stehen.");
                    return this;
                }
            }
        }

        // PHASE 2 – Ziel: Exit
        else {
            if (currentPos.equals(target)) {
                if (agent.tryEnterZone(exitZone)) {
                    agent.setPanicTicks(ticksInPanic);
                    event.getCollector().recordPanicEscape(agent, exitZone);
                    agent.clearTarget();
                    System.out.println("Agent hat Exit betreten");
                    return this;
                } else {
                    Zone alt = event.getNearestAvailableExit(currentPos);
                    if (alt != null && !alt.equals(exitZone)) {
                        exitZone = alt;
                        target = exitZone.getPosition();
                        agent.setTargetPosition(target);
                        System.out.println("Exit blockiert – neuer Exit bei " + target);
                    }
                }
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
