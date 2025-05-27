package org.simulation;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Int2D;

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
