package zones;

import sim.util.Int2D;

/**
 * Repräsentiert eine Zone für den Main Act (Hauptbühne) innerhalb der Simulation.
 * Diese Zone erlaubt Besuchern, sich dort aufzuhalten, um die Hauptveranstaltung zu verfolgen.
 * Sie besitzt eine begrenzte Kapazität für Agenten.
 *
 * @author Dorothea Ziegler
 */
public class MainActZone extends Zone {

    // Konstruktor setzt Position, Kapazität und Zonentyp auf "MainAct"
    public MainActZone(ZoneType type, Int2D position, int capacity) {
        super(type, position, capacity);
    }

}
