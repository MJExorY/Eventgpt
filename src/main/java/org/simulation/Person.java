package org.simulation;

import States.EmergencyState;

import java.awt.Color;

public class Person extends Agent {
    public enum PersonType {
        MEDIC, SECURITY
    }

    private final PersonType type;

    public Person(PersonType type) {
        this.type = type;
        initStateForType();  // Zustand je nach Rolle setzen
    }

    public PersonType getType() {
        return type;
    }

    public void initStateForType() {
        switch (type) {
            case MEDIC -> setCurrentState(new EmergencyState());
            case SECURITY -> setCurrentState(new EmergencyState());
        }
    }

    @Override
    public Color getColor() {
        return switch (type) {
            case MEDIC -> Color.WHITE;
            case SECURITY -> Color.DARK_GRAY;
        };
    }
}

