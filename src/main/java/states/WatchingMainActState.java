package states;

import org.simulation.Agent;
import org.simulation.Event;
import zones.Zone;
import org.simulation.utils.MovementUtils;
import sim.util.Int2D;

import java.util.logging.Level;
import java.util.logging.Logger;

public class WatchingMainActState implements IStates {

    private boolean initialized = false;
    private Int2D target;
    private int ticksInZone = 0;
    private final int WAITTIME = 50;
    private final int showDuration;
    boolean hasEnteredZone = false;
    Logger logger = Logger.getLogger(getClass().getName());

    public WatchingMainActState(Event event) {
        // 300 – 420 Ticks (≈ 5 – 7 Min bei 1 s/Tick)
        this.showDuration = 300 + event.random.nextInt(121);
    }

    @Override
    public IStates act(Agent agent, Event event) {
        if (!initialized) {
            return initialize(agent, event);
        }

        Int2D currentPos = event.grid.getObjectLocation(agent);

        if (!tryEscape(agent, event, currentPos)) {
            return this;
        }

        if (!hasEnteredZone) {
            return handleMovementToZone(agent, event, currentPos);
        }

        return handleStayInZone(agent, event);
    }

    private IStates initialize(Agent agent, Event event) {
        agent.resetFlags();
        agent.setWatchingMain(true);

        Zone mainAct = event.getZoneByType(Zone.ZoneType.ACT_MAIN);
        if (mainAct != null) {
            target = mainAct.getPosition();
            agent.setTargetPosition(target);
            initialized = true;
            return this;
        }
        return new RoamingState(); // fallback
    }

    private boolean tryEscape(Agent agent, Event event, Int2D currentPos) {
        if (!MovementUtils.tryEscapeRestrictedArea(agent, event)) {
            if (logger.isLoggable(Level.INFO)) {
                logger.info(String.format("Agent (MainAct) gefangen in Sperrzone bei %s", currentPos));
            }
            return false;
        }
        return true;
    }

    private IStates handleMovementToZone(Agent agent, Event event, Int2D currentPos) {
        if (currentPos.equals(target)) {
            Zone zone = event.getZoneByPosition(target);
            if (zone != null && agent.tryEnterZone(zone)) {
                hasEnteredZone = true;
                return this;
            }
        }

        boolean moved = MovementUtils.moveAgentTowards(agent, event, target);
        if (!moved && logger.isLoggable(Level.INFO)) {
            logger.info(String.format("Agent (MainAct) blockiert auf dem Weg zu %s", target));
        }

        return this;
    }

    private IStates handleStayInZone(Agent agent, Event event) {
        ticksInZone++;

        MovementUtils.randomMove(agent, event);

        // Nach WAITTIME wieder in Roaming zurück
        if (ticksInZone >= showDuration) {
            Zone zone = agent.getCurrentZone();
            if (zone != null) {
                //  Aufenthaltsdauer melden
                event.getCollector().recordTimeInZone(
                        zone.getType().name(),   // "ACT_MAIN"
                        ticksInZone              // bereits gezählte Ticks
                );
                zone.leave(agent);
                agent.setCurrentZone(null);
            }
            agent.setWatchingMain(false);
            return new RoamingState();
        }

        if (ticksInZone >= WAITTIME) {
            Zone zone = agent.getCurrentZone();
            if (zone != null) {
                zone.leave(agent);
                agent.setCurrentZone(null);
            }
            agent.setWatchingMain(false);
            return new RoamingState();
        }
        return this;
    }

}
