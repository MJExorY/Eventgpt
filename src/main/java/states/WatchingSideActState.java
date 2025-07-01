package states;

import org.simulation.Agent;
import org.simulation.Event;
import zones.Zone;
import org.simulation.utils.MovementUtils;
import sim.util.Int2D;

import java.util.logging.Level;
import java.util.logging.Logger;

public class WatchingSideActState implements IStates {

    private boolean initialized = false;
    private Int2D target;
    private int ticksInZone = 0;
    private final int WAITTIME = 40;
    private final int showDuration;
    private boolean hasEnteredZone = false;
    Logger logger = Logger.getLogger(getClass().getName());


    public WatchingSideActState(Event event) {
        // 300 – 420 Ticks (≈ 5 – 7 Min bei 1 s/Tick)
        this.showDuration = 300 + event.random.nextInt(121);
    }

    @Override
    public IStates act(Agent agent, Event event) {
        if (!initialized) {
            return initialize(agent, event);
        }

        if (!MovementUtils.tryEscapeRestrictedArea(agent, event)) {
            logger.info("Agent (SideAct) weiterhin gefangen in Sperrzone.");
            return this;
        }

        Int2D currentPos = event.grid.getObjectLocation(agent);

        if (!hasEnteredZone) {
            return moveToZone(agent, event, currentPos);
        }

        return stayInZone(agent, event);
    }

    private IStates initialize(Agent agent, Event event) {
        agent.resetFlags();
        agent.setWatchingSide(true);

        Zone sideAct = event.getZoneByType(Zone.ZoneType.ACT_SIDE);
        if (sideAct != null) {
            target = sideAct.getPosition();
            agent.setTargetPosition(target);
            initialized = true;
            return this;
        }
        return new RoamingState(); // fallback
    }

    private IStates moveToZone(Agent agent, Event event, Int2D currentPos) {
        if (currentPos.equals(target)) {
            Zone zone = event.getZoneByPosition(target);
            if (zone != null && agent.tryEnterZone(zone)) {
                hasEnteredZone = true;

            }
        }

        boolean moved = MovementUtils.moveAgentTowards(agent, event, target);
        if (!moved && logger.isLoggable(Level.INFO)) {
            logger.info(String.format("Agent (SideAct) blockiert beim Weg zum Ziel bei %s", target));
        }
        return this;
    }

    private IStates stayInZone(Agent agent, Event event) {
        ticksInZone++;

        boolean moved = MovementUtils.randomMove(agent, event);
        if (!moved && logger.isLoggable(Level.INFO)) {
            logger.info("Agent (SideAct) kann sich in Zone nicht bewegen.");
        }

        if (ticksInZone >= WAITTIME) {
            leaveZone(agent);
            return new RoamingState();
        }
        // Nach WAITTIME wieder in Roaming zurück
        if (ticksInZone >= showDuration) {
            Zone zone = agent.getCurrentZone();
            if (zone != null) {
                //  Aufenthaltsdauer melden
                event.getCollector().recordTimeInZone(
                        zone.getType().name(),   // "ACT_SIDE"
                        ticksInZone
                );
                zone.leave(agent);
                agent.setCurrentZone(null);
            }
            agent.setWatchingSide(false);
            return new RoamingState();
        }

        return this;
    }
    
    private void leaveZone(Agent agent) {
        Zone zone = agent.getCurrentZone();
        if (zone != null) {
            zone.leave(agent);
            agent.setCurrentZone(null);
        }
        agent.setWatchingSide(false);
    }

}
