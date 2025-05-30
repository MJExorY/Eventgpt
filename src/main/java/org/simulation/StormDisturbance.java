package org.simulation;

import sim.engine.SimState;

/**
 * A storm disturbance that affects the entire simulation area.
 * No specific position required.
 */
public class StormDisturbance extends Disturbance {

    public StormDisturbance() {
        super(null); // No position needed
    }

    @Override
    public void step(SimState state) {
        // TODO: Apply global effects, e.g., slow down all agents or increase panic levels
    }

    @Override
    public String getLabel() {
        return "Storm";
    }

    public static StormDisturbance createRandom(Event sim) {
        StormDisturbance storm = new StormDisturbance();

        // Optional: place it at top-left just for visual reference
        sim.grid.setObjectLocation(storm, 0, 0);

        return storm;
    }
}
