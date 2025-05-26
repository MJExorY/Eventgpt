package org.simulation;

// Repräsentiert die Zone für den Main Act (Hauptbühne)
public class MainActZone extends Zone {

    // Konstruktor setzt Position, Kapazität und Zonentyp auf "MainAct"
    public MainActZone(int x, int y, int capacity) {
        super(x, y, capacity, "MainAct");
    }

    // Einfaches Verhalten beim Interagieren mit dieser Zone
    @Override
    public void interact() {
        System.out.println("Interacting with MainAct. Capacity: " + capacity);
    }
}
