package org.simulation;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Int2D;

public class Agent implements Steppable {

    @Override
    public void step(SimState state) {
        Event sim = (Event) state;

        // Aktuelle Position abfragen
        Int2D pos = sim.grid.getObjectLocation(this);

        // Zufällige Bewegung (-1, 0 oder +1)
        int dx = sim.random.nextInt(3) - 1;
        int dy = sim.random.nextInt(3) - 1;

        // Neue Position berechnen (inkl. Randbegrenzung)
        int newX = Math.max(0, Math.min(sim.grid.getWidth() - 1, pos.x + dx));
        int newY = Math.max(0, Math.min(sim.grid.getHeight() - 1, pos.y + dy));

        // Neue Position setzen
        sim.grid.setObjectLocation(this, newX, newY);
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
