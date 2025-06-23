package org.simulation;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Int2D;

/**
 * Repräsentiert einen festen Notausgang auf der rechten Seite des Geländes.
 */
public class EmergencyRootRechts implements Steppable {

    private final Int2D position;

    public EmergencyRootRechts(Int2D position) {
        this.position = position;
    }

    public Int2D getPosition() {
        return position;
    }

    @Override
    public void step(SimState state) {
        // Keine Logik notwendig – der Ausgang ist statisch
    }

    @Override
    public String toString() {
        return "EmergencyRootRechts @ " + position;
    }
}