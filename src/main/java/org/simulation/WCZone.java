package org.simulation;

import sim.util.Int2D;

// Repräsentiert die Zone für Toilettenbereiche
public class WCZone extends Zone {

    // Konstruktor setzt Position, Kapazität und Zonentypp auf "WC"

    public WCZone(ZoneType type, Int2D position, int capacity) {
        super(type, position, capacity);
    }

}
