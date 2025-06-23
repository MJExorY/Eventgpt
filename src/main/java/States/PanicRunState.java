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

    private boolean initialized = false;
    private boolean reachedEmergencyRoute = false;

    private Int2D target;
    private Zone exitZone;

    @Override
    public IStates act(Agent agent, Event event) {
        Int2D currentPos = event.grid.getObjectLocation(agent);

        // Initialisierung: Panikmodus aktivieren
        if (!initialized) {
            agent.resetFlags();
            agent.setPanicking(true);
            initialized = true;
        }

        // PHASE 1: EmergencyRoute finden
        if (!reachedEmergencyRoute) {
            if (target == null) {
                Bag allObjects = event.grid.getAllObjects();

                Int2D nearest = null;
                int minDistance = Integer.MAX_VALUE;

                for (Object o : allObjects) {
                    Int2D pos = null;

                    if (o instanceof EmergencyRouteRechts) {
                        pos = ((EmergencyRouteRechts) o).getPosition();
                        if (currentPos.equals(pos)) {
                            Zone exit = event.getNearestAvailableExit(pos);
                            if (exit != null) {
                                exitZone = exit;
                                target = exit.getPosition();
                                agent.setTargetPosition(target);
                                reachedEmergencyRoute = true;
                                System.out.println("Agent steht bereits auf Route (Rechts) – Exit-Ziel gesetzt!");
                                break;
                            }
                        }
                    } else if (o instanceof EmergencyRouteLinks) {
                        pos = new Int2D(20, 50);
                        if (currentPos.equals(pos)) {
                            Zone exit = event.getNearestAvailableExit(pos);
                            if (exit != null) {
                                exitZone = exit;
                                target = exit.getPosition();
                                agent.setTargetPosition(target);
                                reachedEmergencyRoute = true;
                                System.out.println("Agent steht bereits auf Route (Links) – Exit-Ziel gesetzt!");
                                break;
                            }
                        }
                    } else if (o instanceof EmergencyRouteStraight) {
                        pos = new Int2D(50, 20);
                        if (currentPos.equals(pos)) {
                            Zone exit = event.getNearestAvailableExit(pos);
                            if (exit != null) {
                                exitZone = exit;
                                target = exit.getPosition();
                                agent.setTargetPosition(target);
                                reachedEmergencyRoute = true;
                                System.out.println("Agent steht bereits auf Route (Straight) – Exit-Ziel gesetzt!");
                                break;
                            }
                        }
                    }

                    if (pos != null) {
                        int dist = Math.abs(pos.x - currentPos.x) + Math.abs(pos.y - currentPos.y);
                        if (dist < minDistance) {
                            minDistance = dist;
                            nearest = pos;
                        }
                    }
                }

                // Falls Agent NICHT direkt auf Route stand → nächstgelegene Route ansteuern
                if (!reachedEmergencyRoute && nearest != null) {
                    target = nearest;
                    agent.setTargetPosition(target);
                    System.out.println("Ziel gesetzt: EmergencyRoute bei " + target);
                }

                // Falls gar nichts gefunden → zurück zu Roaming
                if (target == null) {
                    System.out.println("Keine Emergency Route gefunden – zurück zu Roaming");
                    return new RoamingState();
                }
            }

            // Wenn Ziel erreicht wurde
            if (currentPos.equals(target)) {
                reachedEmergencyRoute = true;
                System.out.println("EmergencyRoute erreicht – wechsle zu Exit");

                exitZone = event.getNearestAvailableExit(currentPos);
                if (exitZone != null) {
                    target = exitZone.getPosition();
                    agent.setTargetPosition(target);
                    System.out.println("Neuer Zielpunkt: Exit bei " + target);
                } else {
                    System.out.println("Kein Exit verfügbar – Agent bleibt stehen.");
                }
            }

        } else {
            // PHASE 2: Agent läuft zum Exit
            if (target == null && exitZone != null) {
                target = exitZone.getPosition();
                agent.setTargetPosition(target);
            }

            if (currentPos.equals(target)) {
                if (agent.tryEnterZone(exitZone)) {
                    agent.clearTarget();
                    System.out.println("Agent hat Exit betreten");
                    return this;
                } else {
                    Zone alternative = event.getNearestAvailableExit(currentPos);
                    if (alternative != null && !alternative.equals(exitZone)) {
                        exitZone = alternative;
                        target = exitZone.getPosition();
                        agent.setTargetPosition(target);
                        System.out.println("Exit blockiert – neuer Exit bei " + target);
                    }
                }
            }
        }

        if (target != null && !currentPos.equals(target)) {
            moveAgent(agent, currentPos, target, event);
        }

        return this;
    }

    private void moveAgent(Agent agent, Int2D currentPos, Int2D target, Event event) {
        int dx = Integer.compare(target.x, currentPos.x);
        int dy = Integer.compare(target.y, currentPos.y);
        int newX = Math.max(0, Math.min(event.grid.getWidth() - 1, currentPos.x + dx));
        int newY = Math.max(0, Math.min(event.grid.getHeight() - 1, currentPos.y + dy));
        event.grid.setObjectLocation(agent, new Int2D(newX, newY));
        System.out.println("→ Agent bewegt sich zu: (" + newX + ", " + newY + ")");
    }
}
