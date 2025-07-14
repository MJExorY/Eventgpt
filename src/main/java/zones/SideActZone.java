package zones;

import sim.util.Int2D;

/**
 * Repräsentiert eine Zone für den Side Act (Nebenbühne) innerhalb der Simulation.
 * Diese Zone erlaubt Besuchern, sich dort aufzuhalten, um die Nebenveranstaltung zu verfolgen.
 * Sie besitzt eine begrenzte Kapazität für Agenten.
 *
 * @author Dorothea Ziegler
 */
public class SideActZone extends Zone {

    // Konstruktor setzt Position, Kapazität und Zonentyp auf "SideAct"

    public SideActZone(ZoneType type, Int2D position, int capacity) {
        super(type, position, capacity);
    }


}
