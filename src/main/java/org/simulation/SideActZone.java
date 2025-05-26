package org.simulation;

import sim.util.Int2D;

// Repräsentiert die Zone für den Side Act (Nebenbühne)
public class SideActZone extends Zone{

    // Konstruktor setzt Position, Kapazität und Zonentyp auf "SideAct"

    public SideActZone(ZoneType type, Int2D position, int capacity) {
        super(type, position, capacity);
    }


}
