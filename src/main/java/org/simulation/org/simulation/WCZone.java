package org.simulation;

// Repräsentiert die Zone für Toilettenbereiche
public class WCZone extends Zone {

    // Konstruktor setzt Position, Kapazität und Zonentypp auf "WC"
    public WCZone(int x, int y, int capacity) {
        super(x, y, capacity, "WC");
    }

    // Einfaches Verhalten beim Interagieren mit dieser Zone
    @Override
    public void interact() {
        System.out.println("Interacting with WC. Capacity: " + capacity);
    }
}
