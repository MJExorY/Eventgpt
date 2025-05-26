package org.simulation;

import sim.util.Int2D;

// Repräsentiert die Zone für den Main Act (Hauptbühne)
public class MainActZone extends Zone {

    // Konstruktor setzt Position, Kapazität und Zonentyp auf "MainAct"
    public MainActZone(ZoneType type, Int2D position, int capacity) {
        super(type, position, capacity);
    }

}
