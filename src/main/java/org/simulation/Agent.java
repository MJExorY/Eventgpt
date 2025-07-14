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

/**
 * Repräsentiert einen einzelnen Agenten innerhalb der Simulation.
 * Der Agent hat einen Zustand, eine Zielposition und Zustandsflags (z. B. hungrig, in Panik, in Warteschlange).
 * Er bewegt sich über das Grid und wechselt seinen Zustand anhand des StatePatterns basierend auf Zonen, Ereignissen oder Regeln.
 *
 * @author Lukas Kilian
 */
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
    private int panicTicks = 0;
    private Event event;
    private Stoppable stopper;
    private boolean alarmed = false;
    private Zone assignedEmergencyExit;
    private static final Logger logger = Logger.getLogger(Agent.class.getName());

    //Aufenthaltsdauer in Exit-Zone (in Ticks)
    private static final int EXIT_DURATION_TICKS = 3;

    // Getter & Setter
    public int getPanicTicks() {
        return panicTicks;
    }

    public void setPanicTicks(int panicTicks) {
        this.panicTicks = panicTicks;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

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
        alarmed = false;
    }


    // Getter & Setter
    public boolean isHungry() {
        return isHungry;
    }

    public void setHungry(boolean hungry) {
        this.isHungry = hungry;
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

    public void clearTarget() {
        targetPosition = null;
    }

    public Zone.ZoneType getLastVisitedZone() {
        return lastVisitedZone;
    }

    public void setLastVisitedZone(Zone.ZoneType type) {
        this.lastVisitedZone = type;
    }

    public Zone getAssignedEmergencyExit() {
        return assignedEmergencyExit;
    }

    public void setAssignedEmergencyExit(Zone exit) {
        this.assignedEmergencyExit = exit;
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

    @Override
    public void step(SimState state) {
        Event sim = (Event) state;
        currentState = currentState.act(this, sim);
        if (!sim.agents.contains(this)) return;

        Int2D pos = sim.grid.getObjectLocation(this);
        if (pos == null) return;

        sim.grid.setObjectLocation(this, pos);

        if (targetPosition == null) {
            int dx = sim.random.nextInt(3) - 1;
            int dy = sim.random.nextInt(3) - 1;
            sim.grid.setObjectLocation(this, new Int2D(
                    Math.max(0, Math.min(sim.grid.getWidth() - 1, pos.x + dx)),
                    Math.max(0, Math.min(sim.grid.getHeight() - 1, pos.y + dy))
            ));
        }

        Zone zone = sim.getZoneByPosition(pos);
        if (currentState == null) {
            // Agent entfernt sich aus der Simulation (wurde von ExitFinalizedState oder QueueingState entschieden)
            if (stopper != null) stopper.stop();
            sim.grid.remove(this);
            sim.agents.remove(this);
            System.out.println("Agent verlässt Simulation aus " + (zone != null ? zone.getType() : "unbekannt"));
        }
    }

    public boolean tryEnterZone(Zone targetZone) {
        if (!targetZone.isFull()) {
            if (currentZone != null) {
                event.getCollector().recordZoneExit(this, currentZone);
                currentZone.leave(this);
            }
            targetZone.enter(this);
            event.getCollector().recordZoneEntry(this, targetZone);
            currentZone = targetZone;
            clearTarget();
            isInQueue = false;

            if (targetZone.getType() == Zone.ZoneType.EMERGENCY_EXIT || targetZone.getType() == Zone.ZoneType.EXIT) {
                event.schedule.scheduleOnce(
                        event.schedule.getTime() + EXIT_DURATION_TICKS,
                        new Steppable() {
                            @Override
                            public void step(SimState sim) {
                                leaveCurrentZone();
                                if (stopper != null) stopper.stop();
                                event.grid.remove(Agent.this);
                                event.agents.remove(Agent.this);
                                System.out.println("Agent hat die Zone " + targetZone.getType() + " verlassen und wurde entfernt.");
                            }
                        }
                );
            }

            return true;
        }
        return false;
    }

    public void leaveCurrentZone() {
        if (currentZone != null) {
            currentZone.leave(this);
            currentZone = null;
        }
    }
}