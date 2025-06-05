package org.simulation;

import sim.engine.SimState;
import sim.util.Int2D;

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

        int x = sim.random.nextInt(sim.grid.getWidth());
        int y = sim.random.nextInt(sim.grid.getHeight());
        sim.grid.setObjectLocation(storm, new Int2D(x, y));

        return storm;
    }
}
