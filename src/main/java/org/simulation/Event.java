package org.simulation;

import sim.engine.SimState;
import sim.field.grid.SparseGrid2D;

public class Event extends SimState {
    public SparseGrid2D grid;

    public Event(long seed) {
        super(seed);
    }

    @Override
    public void start() {
        super.start();
        grid = new SparseGrid2D(100, 100);

        Agent agent = new Agent();
        grid.setObjectLocation(agent, 50, 50); // In die Mitte setzen
        schedule.scheduleRepeating(agent);

    }

    public static void main(String[] args) {
        Event sim = new Event(System.currentTimeMillis());
        sim.start();

        for (int i = 0; i < 10; i++) {
            sim.schedule.step(sim);
        }

        sim.finish();
        System.out.println("Event-Simulation fertig.");
    }
}
