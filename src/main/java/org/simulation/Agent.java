package org.simulation;

import States.IStates;
import States.RoamingState;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Int2D;

import java.awt.*;

public class Agent implements Steppable {
    private IStates currentState = new RoamingState(); // Startzustand
    private Int2D targetPosition = null;
    private boolean isHungry = false;
    private boolean isWatching = false;
    private boolean isInQueue = false;
    private boolean isPanicking = false;
    private boolean isSeeking = false;
    private Zone currentZone = null;
    private Zone.ZoneType lastVisitedZone = null;


    public Zone getCurrentZone() {
        return currentZone;
    }

    public void setCurrentZone(Zone zone) {
        this.currentZone = zone;
    }


    public void resetFlags() {
        isWatching = false;
        isInQueue = false;
        isPanicking = false;
        isSeeking = false;
        isHungry = false;
        currentZone = null;
    }

    // Getter & Setter
    public boolean isHungry() {
        return isHungry;
    }

    public void setHungry(boolean hungry) {
        this.isHungry = hungry;
    }

    public boolean isWatching() {
        return isWatching;
    }

    public void setWatching(boolean watching) {
        this.isWatching = watching;
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
        this.isPanicking = panicking;
    }

    public boolean isSeeking() {
        return isSeeking;
    }

    public void setSeeking(boolean seeking) {
        this.isSeeking = seeking;
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
        if (currentZone != null) {
            switch (currentZone.getType()) {
                case FOOD:
                    return Color.GREEN;
                case ACT_MAIN:
                    return Color.BLUE;
                case ACT_SIDE:
                    return Color.CYAN;
                case EXIT:
                    return Color.GRAY;
            }
        }
        if (isInQueue) return Color.ORANGE;
        if (isSeeking) return Color.MAGENTA;
        return Color.YELLOW; // Roaming
    }


    @Override
    public void step(SimState state) {
        Event sim = (Event) state;

        // 1. Zustandslogik ausführen
        if (currentState != null) {
            currentState = currentState.act(this, sim);
        }

        // Trigger GUI-Update durch "Bewegung zu gleicher Position"
        Int2D pos = sim.grid.getObjectLocation(this);
        sim.grid.setObjectLocation(this, pos); // erzwingt Repaint

        // 2. Wenn kein Ziel → zufaellige Bewegung (roaming)
        if (targetPosition == null) {
            pos = sim.grid.getObjectLocation(this);
            int dx = sim.random.nextInt(3) - 1; // -1, 0, +1
            int dy = sim.random.nextInt(3) - 1;
            int newX = Math.max(0, Math.min(sim.grid.getWidth() - 1, pos.x + dx));
            int newY = Math.max(0, Math.min(sim.grid.getHeight() - 1, pos.y + dy));
            sim.grid.setObjectLocation(this, new Int2D(newX, newY));
        }
        System.out.println("Agent @ " + sim.grid.getObjectLocation(this)
                + " | State: " + currentState.getClass().getSimpleName()
                + " | target: " + targetPosition);
    }
}

class Person implements Steppable {
    private Int2D position;

    public Person(Int2D position) {
        this.position = position;
    }

    public Int2D getPosition() {
        return position;
    }

    public void setPosition(Int2D position) {
        this.position = position;
    }


    @Override
    public void step(SimState simState) {

    }
}
