package org.simulation;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Int2D;

import java.awt.Color;

class Person implements Steppable {

    public enum PersonType {
        MEDIC, SECURITY
    }

    private Int2D position;
    private final PersonType type;

    public Person(Int2D position, PersonType type) {
        this.position = position;
        this.type = type;
    }

    public Int2D getPosition() {
        return position;
    }

    public void setPosition(Int2D position) {
        this.position = position;
    }

    public PersonType getType() {
        return type;
    }

    public Color getColor() {
        return switch (type) {
            case MEDIC -> Color.WHITE;
            case SECURITY -> Color.DARK_GRAY;
        };
    }

    @Override
    public void step(SimState simState) {

    }
}
