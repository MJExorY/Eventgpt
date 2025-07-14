package states;

import org.simulation.Agent;
import org.simulation.Event;
import zones.Zone;
import org.simulation.utils.MovementUtils;
import sim.util.Int2D;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * State Pattern Zustand für Agenten, die hungrig oder durstig sind und versuchen,
 * eine FOOD-Zone zu erreichen, dort Zeit zu verbringen und danach wieder zu roam-en.
 * Bei überfüllter Zone wechselt der Agent in den QueueingState.
 * Die Verweildauer in der Zone wird zufällig (zwischen 90–180 Ticks) bestimmt.
 * Dieser Zustand ist nur temporär.
 *
 * @author Burak Tamer
 */
public class HungryThirstyState implements IStates {

    private boolean initialized = false;
    private boolean enteredZone = false;
    private Int2D target;
    private int ticksInZone = 0;
    Logger logger = Logger.getLogger(getClass().getName());
    private final int eatTime;

    // Konstruktor erhält das Event-Objekt, um an den Zufallsgenerator zu kommen
    public HungryThirstyState(Event event) {
        // 90–180 Ticks (1,5–3 Min bei 1 s/Tick)
        this.eatTime = 90 + event.random.nextInt(91);
    }

    @Override
    public IStates act(Agent agent, Event event) {
        Int2D currentPos = event.grid.getObjectLocation(agent);

        // 1. Initiales Ziel setzen
        if (!initialized) {
            agent.resetFlags();
            agent.setHungry(true);

            Zone foodZone = event.getZoneByType(Zone.ZoneType.FOOD);
            if (foodZone != null) {
                target = foodZone.getPosition();
                agent.setTargetPosition(target);
                initialized = true;
            } else {
                return new RoamingState(); // fallback
            }
        }

        // 2. Noch nicht in der Food-Zone?
        if (!enteredZone) {
            if (currentPos.equals(target)) {
                Zone zone = event.getZoneByPosition(target);
                if (zone != null) {
                    if (agent.tryEnterZone(zone)) {
                        enteredZone = true;
                    } else {
                        // Food-Zone ist VOLL → in Queue übergehen
                        return new QueueingState(agent, zone, this);
                    }
                } else {
                    return new RoamingState();
                }
            } else {
                // → NEU: alles Movement auslagern
                boolean escaped = MovementUtils.tryEscapeRestrictedArea(agent, event);
                if (!escaped) {
                    if (logger.isLoggable(Level.INFO)) {
                        logger.info(String.format("Agent (hungrig) steckt fest in Sperrzone: %s", currentPos));
                    }
                    return this;
                }

                MovementUtils.moveAgentTowards(agent, event, target);
            }
        } else {
            // 3. In der Zone → Wartezeit simulieren
            ticksInZone++;

            if (ticksInZone >= eatTime) {
                Zone zone = agent.getCurrentZone();
                if (zone != null) {
                    event.getCollector().recordTimeInZone(zone.getType().name(), ticksInZone);
                    zone.leave(agent);
                    agent.setCurrentZone(null);
                }
                agent.setHungry(false);
                return new RoamingState();
            }
        }

        return this;
    }
}
