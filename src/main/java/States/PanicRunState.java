package States;

import org.simulation.Agent;
import org.simulation.EmergencyRouteLinks;
import org.simulation.EmergencyRouteRechts;
import org.simulation.EmergencyRouteStraight;
import org.simulation.Event;
import org.simulation.Zone;
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

        // Initialisierung nur einmal
        if (!agent.isPanicking()) {
            agent.resetFlags();
            agent.setPanicking(true);
        }

        ticksInPanic++;
        // PHASE 1 – Ziel: EmergencyRoute
        if (!reachedEmergencyRoute) {
            if (target == null) {
                Int2D nearest = null;
                int minDist = Integer.MAX_VALUE;

                Bag all = event.grid.getAllObjects();
                for (Object o : all) {
                    Int2D pos = null;

                    if (o instanceof EmergencyRouteRechts) {
                        pos = ((EmergencyRouteRechts) o).getPosition();
                    } else if (o instanceof EmergencyRouteLinks) {
                        pos = new Int2D(20, 50); // HARDCODED wie im Grid
                    } else if (o instanceof EmergencyRouteStraight) {
                        pos = new Int2D(50, 20); // HARDCODED wie im Grid
                    }

                    if (pos != null) {
                        int dist = Math.abs(pos.x - currentPos.x) + Math.abs(pos.y - currentPos.y);
                        if (dist < minDist) {
                            minDist = dist;
                            nearest = pos;
                        }
                    }
                }

                if (nearest != null) {
                    target = nearest;
                    agent.setTargetPosition(target);
                    System.out.println("Ziel gesetzt: EmergencyRoute bei " + target);
                } else {
                    System.out.println("Keine Emergency Route gefunden – zurück zu Roaming");
                    return new RoamingState();
                }
            }

            // Route erreicht?
            if (currentPos.equals(target)) {
                reachedEmergencyRoute = true;
                System.out.println("EmergencyRoute erreicht – wechsle zu Exit");

                exitZone = event.getNearestAvailableExit(currentPos);
                if (exitZone != null) {
                    target = exitZone.getPosition();
                    agent.setTargetPosition(target);
                    System.out.println("Neuer Zielpunkt: Exit bei " + target);

                    moveAgent(agent, currentPos, target, event);
                    return this; // WICHTIG: danach abbrechen
                } else {
                    System.out.println("⚠ Kein Exit verfügbar – Agent bleibt stehen.");
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

        moveAgent(agent, currentPos, target, event);
        return this;
    }

    private void moveAgent(Agent agent, Int2D currentPos, Int2D target, Event event) {
        int dx = Integer.compare(target.x, currentPos.x);
        int dy = Integer.compare(target.y, currentPos.y);
        int newX = Math.max(0, Math.min(event.grid.getWidth() - 1, currentPos.x + dx));
        int newY = Math.max(0, Math.min(event.grid.getHeight() - 1, currentPos.y + dy));
        Int2D newPos = new Int2D(newX, newY);
        event.grid.setObjectLocation(agent, newPos);
    }
}
