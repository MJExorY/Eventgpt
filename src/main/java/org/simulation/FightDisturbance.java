package org.simulation;

import sim.engine.SimState;
import sim.util.Int2D;

/**
 * A fight disturbance at a specific position.
 * May cause aggressive reactions or draw security agents.
 */
public class FightDisturbance extends Disturbance {

    public FightDisturbance(Int2D position) {
        super(position);
    }

    @Override
    public void step(SimState state) {
        // TODO: Affect agents nearby with fight-related behavior
    }

    @Override
    public String getLabel() {
        return "Fight";
    }

    public static FightDisturbance createRandom(Event sim) {
        int x = sim.random.nextInt(sim.grid.getWidth());
        int y = sim.random.nextInt(sim.grid.getHeight());
        return new FightDisturbance(new Int2D(x, y));
    }
}
