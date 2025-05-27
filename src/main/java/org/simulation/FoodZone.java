package org.simulation;

import sim.util.Int2D;

// Repräsentiert die Zone für den Essenstand
public class FoodZone extends Zone{

    // Konstruktor setzt Position, Kapazität und Zonentyp auf "Food"
    public FoodZone(ZoneType type, Int2D position, int capacity) {
        super(type, position, capacity);
    }


}
