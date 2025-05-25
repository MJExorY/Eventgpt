package org.simulation;

// Repräsentiert die Zone für den Essenstand
public class FoodZone extends Zone{

    // Konstruktor setzt Position, Kapazität und Zonentyp auf "Food"
    public FoodZone(int x, int y, int capacity) {
        super(x, y, capacity,"Food");
    }

    // Einfaches Verhalten beim Interagieren mit dieser Zone
    @Override
    public void interact() {
        System.out.println("Interacting with Food. Capacity: " + capacity);
    }
}
