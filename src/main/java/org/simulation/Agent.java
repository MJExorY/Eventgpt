package org.simulation;

import states.IStates;
import states.RoamingState;
import org.simulation.utils.MovementUtils;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;

import java.util.logging.Level;
import java.util.logging.Logger;

import sim.util.Int2D;
import zones.Zone;

import java.awt.*;

public class Agent implements Steppable {
    private IStates currentState = new RoamingState(); // Startzustand
    private Int2D targetPosition = null;
    private boolean isRoaming = false;
    private boolean isHungry = false;
    private boolean isWatchingMain = false;
    private boolean isWatchingSide = false;
    private boolean isInQueue = false;
    private boolean isPanicking = false;
    private boolean isWC = false;
    private Zone currentZone = null;
    private Zone.ZoneType lastVisitedZone = null;
    private static final Logger logger = Logger.getLogger(Agent.class.getName());

    public int getPanicTicks() {
        return panicTicks;
    }

    public void setPanicTicks(int panicTicks) {
        this.panicTicks = panicTicks;
    }

    private int panicTicks = 0;

    private Event event;

    public void setEvent(Event event) {
        this.event = event;
    }

    private Stoppable stopper;

    public void setStopper(Stoppable stopper) {
        this.stopper = stopper;
    }

    public Stoppable getStopper() {
        return stopper;
    }

    public Zone getCurrentZone() {
        return currentZone;
    }

    public void setCurrentZone(Zone zone) {
        this.currentZone = zone;
    }

    private long queueStartTick = -1;

    public void resetFlags() { // Setzt Zustand zurück - wichtig für State wechsel
        isWatchingMain = false;
        isWatchingSide = false;
        isInQueue = false;
        isPanicking = false;
        isHungry = false;
        isWC = false;
        currentZone = null;
        panicTicks = 0;
    }


    // Getter & Setter
    public boolean isHungry() {
        return isHungry;
    }

    public boolean isWC() {
        return isWC;
    }

    public void setWC(boolean WC) {
        isWC = WC;
    }

    public boolean isRoaming() {
        return isRoaming;
    }

    public void setRoaming(boolean roaming) {
        isRoaming = roaming;
    }

    public void setHungry(boolean hungry) {
        this.isHungry = hungry;
    }

    public boolean isWatchingMain() {
        return isWatchingMain;
    }

    public void setWatchingMain(boolean watchingMain) {
        this.isWatchingMain = watchingMain;
    }

    public boolean isWatchingSide() {
        return isWatchingSide;
    }

    public void setWatchingSide(boolean watchingSide) {
        this.isWatchingSide = watchingSide;
    }

    public boolean isInQueue() {
        return isInQueue;
    }

    public void setInQueue(boolean inQueue) {
        this.isInQueue = inQueue;
    }

    public boolean isPanicking() {
        return isPanicking;
    }

    public void setPanicking(boolean panicking) {
        isPanicking = panicking;
    }

    public IStates getCurrentState() {
        return currentState;
    }

    public void setCurrentState(IStates state) {
        this.currentState = state;
    }

    public Int2D getTargetPosition() {
        return targetPosition;
    }

    public void setTargetPosition(Int2D position) {
        if (position == null) {
            this.targetPosition = null;
            return;
        }

        if (event != null) {
            for (RestrictedArea ra : event.getRestrictedAreas()) {
                if (ra.isActive()
                        && ra.isInside(position.x, position.y)
                        && (!(this instanceof Person person) || person.getType() == Person.PersonType.VISITOR)) {

                    if (logger.isLoggable(Level.INFO)) {
                        logger.info(String.format("Ziel liegt in Sperrzone und wird ignoriert: %s", position));
                    }
                    return;
                }

            }
        }

        this.targetPosition = position;
    }

    public boolean hasTarget() {
        return targetPosition != null;
    }

    public void clearTarget() {
        targetPosition = null;
    }

    public Zone.ZoneType getLastVisitedZone() {
        return lastVisitedZone;
    }

    public void setLastVisitedZone(Zone.ZoneType type) {
        this.lastVisitedZone = type;
    }

    public Color getColor() {
        if (isPanicking) return Color.RED;
        if (isHungry) return Color.GREEN;
        if (isWatchingMain) return Color.BLUE;
        if (isWatchingSide) return Color.CYAN;
        if (isInQueue) return Color.MAGENTA;
        if (isWC) return Color.PINK;
        if (isRoaming) return Color.YELLOW;
        return Color.YELLOW; // Roaming
    }

    public void setQueueStartTick(long tick) {
        this.queueStartTick = tick;
    }

    public long getQueueStartTick() {
        return queueStartTick;
    }

    public void resetQueueStartTick() {
        this.queueStartTick = -1;
    }


    @Override
    public void step(SimState state) {
        Event sim = (Event) state;

        // Aktuellen Zustand ausführen
        if (currentState != null) {
            currentState = currentState.act(this, sim);
        }

        Int2D pos = sim.grid.getObjectLocation(this);
        if (pos == null) return;

        if (!MovementUtils.tryEscapeRestrictedArea(this, sim)) {
            return;
        }

        if (!handlePossibleRemoval(sim, pos)) {
            handleMovement(sim);
        }

        logStatus(pos);
    }

    private boolean handlePossibleRemoval(Event sim, Int2D pos) {
        Zone currentzone = sim.getZoneByPosition(pos);
        if (currentzone == null) {
            return false;
        }

        boolean mustRemove = currentzone.getType() == Zone.ZoneType.EXIT ||
                (currentzone.getType() == Zone.ZoneType.EMERGENCY_EXIT && isPanicking());

        if (mustRemove) {
            if (logger.isLoggable(Level.INFO)) {
                logger.info(String.format(
                        "Agent hat %s erreicht und wird entfernt: %s",
                        currentzone.getType(), pos
                ));
            }
            if (stopper != null) stopper.stop();
            sim.grid.remove(this);
            sim.agents.remove(this);
            return true;
        }
        return false;
    }

    private void handleMovement(Event sim) {
        if (getTargetPosition() != null) {
            boolean moved = MovementUtils.moveAgentTowards(this, sim, getTargetPosition());
            if (!moved && logger.isLoggable(Level.INFO)) {
                logger.info(String.format("🚫 Agent blockiert auf dem Weg zu Ziel: %s", getTargetPosition()));
            }
        } else {
            MovementUtils.randomMove(this, sim);
        }
    }

    private void logStatus(Int2D pos) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info(String.format(
                    "Agent @ %s | State: %s | target: %s",
                    pos,
                    currentState != null ? currentState.getClass().getSimpleName() : "null",
                    targetPosition
            ));
        }
    }


    private boolean alarmed = false;

    public boolean isAlarmed() {
        return alarmed;
    }

    public void setAlarmed(boolean alarmed) {
        this.alarmed = alarmed;
    }

    public boolean tryEnterZone(Zone targetZone) {
        if (!targetZone.isFull()) {
            if (currentZone != null) {
                event.getCollector().recordZoneExit(this, currentZone);
                currentZone.leave(this);
            }

            targetZone.enter(this);
            event.getCollector().recordZoneEntry(this, targetZone);
            setCurrentZone(targetZone);
            setLastVisitedZone(targetZone.getType());
            clearTarget();
            setInQueue(false);
            return true;
        }
        return false;
    }
}
