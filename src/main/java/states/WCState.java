package states;

import org.simulation.Agent;
import org.simulation.Event;

import sim.util.Int2D;
import zones.Zone;

/**
 * Zustand für den Besuch der WC-Zone.
 *
 * @author Burak Tamer
 */

public class WCState implements IStates {

    protected boolean initialized = false;
    protected boolean enteredZone = false;
    private Int2D target;
    private int ticksInZone = 0;
    private final int waitTime;

    public WCState(Event event) {
        // 120 – 240 Ticks  (~ 2 – 4 Min bei 1 s/Tick)
        this.waitTime = 120 + event.random.nextInt(121);
    }

    @Override
    public IStates act(Agent agent, Event event) {
        Int2D currentPos = event.grid.getObjectLocation(agent);

        // Initialisierung (einmalig)
        if (!initialized) {
            if (!agent.isInQueue()) {
                agent.resetFlags(); // Nur wenn kein Queueing aktiv ist
            }

            agent.setWC(true);

            Zone wcZone = event.getZoneByType(Zone.ZoneType.WC);
            if (wcZone != null) {
                target = wcZone.getPosition();
                agent.setTargetPosition(target);
                initialized = true;
            } else {
                return new RoamingState(); // fallback
            }
        }

        // Noch nicht drin?
        if (!enteredZone) {
            if (currentPos.equals(target)) {
                Zone zone = event.getZoneByPosition(target);
                if (zone != null) {
                    if (agent.tryEnterZone(zone)) {
                        enteredZone = true;
                        agent.setInQueue(false); // Queue erfolgreich beendet
                    } else {
                        // WC voll → Agent wartet (Queueing)
                        return new QueueingState(agent, zone, this);
                    }
                } else {
                    return new RoamingState(); // Position unklar
                }
            } else {
                // Bewegung zur Zielposition
                int dx = Integer.compare(target.x, currentPos.x);
                int dy = Integer.compare(target.y, currentPos.y);
                int newX = Math.max(0, Math.min(event.grid.getWidth() - 1, currentPos.x + dx));
                int newY = Math.max(0, Math.min(event.grid.getHeight() - 1, currentPos.y + dy));
                event.grid.setObjectLocation(agent, new Int2D(newX, newY));
            }
        } else {
            // Ist in der WC-Zone - Time
            ticksInZone++;
            if (ticksInZone >= waitTime) {
                Zone zone = agent.getCurrentZone();
                if (zone != null) {
                    event.getCollector().recordTimeInZone(zone.getType().name(), ticksInZone);
                    zone.leave(agent);
                    agent.setCurrentZone(null);
                }
                agent.setWC(false);
                return new RoamingState();
            }
        }

        return this;
    }
}
