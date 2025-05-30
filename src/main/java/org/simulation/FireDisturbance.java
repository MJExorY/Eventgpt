package org.simulation;

import sim.engine.SimState;
import sim.util.Int2D;

/**
 * A fire disturbance at a specific position.
 * Can trigger panic behavior in nearby agents.
 */
public class FireDisturbance extends Disturbance {

    public FireDisturbance(Int2D position) {
        super(position);
    }

    @Override
    public void step(SimState state) {
        // TODO: Trigger panic behavior in surrounding agents
    }

    @Override
    public String getLabel() {
        return "Fire";
    }

    public static FireDisturbance createRandom(Event sim) {
        int x = sim.random.nextInt(sim.grid.getWidth());
        int y = sim.random.nextInt(sim.grid.getHeight());
        return new FireDisturbance(new Int2D(x, y));
    }
}
