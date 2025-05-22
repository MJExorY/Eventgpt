package org.simulation;

// Repräsentiert die Zone für den Side Act (Nebenbühne)
public class SideActZone extends Zone{

    // Konstruktor setzt Position, Kapazität und Zonentyp auf "SideAct"
    public SideActZone(int x, int y, int capacity) {
        super(x, y, capacity, "SideAct");
    }

    // Einfaches Verhalten beim Interagieren mit dieser Zone
    @Override
    public void interact() {
        System.out.println("Interacting with SideAct. Capacity: " + capacity);
    }
}
