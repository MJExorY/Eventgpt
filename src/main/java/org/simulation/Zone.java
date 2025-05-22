package org.simulation;

public abstract class Zone {
    // Position im Grid
    protected int x, y;

    // Kapazität der Zone
    protected int capacity;

    // Typ der Zone ("MainAct", "SideAct", "Food", "WC")
    protected String label;

    // Konstruktor
    public Zone(int x, int y, int capacity, String label) {
        this.x = x;
        this.y = y;
        this.capacity = capacity;
        this.String = label;
    }

    // Getter-Methoden
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getCapacity() {
        return capacity;
    }

    public String getLabel() {
        return label;
    }

    // Abstrakte Methode für Interaktionen.
    public abstract void interact();
}
