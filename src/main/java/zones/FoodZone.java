package zones;

import sim.util.Int2D;

/**
 * Repräsentiert eine Food-Zone (Essensstand) innerhalb der Simulation.
 * Diese Zone erlaubt Agenten, sich dort aufzuhalten, um zu essen,
 * und verfügt über eine feste Kapazität.
 *
 * @author Dorothea Ziegler
 */

// Repräsentiert die Zone für den Essenstand
public class FoodZone extends Zone {

    // Konstruktor setzt Position, Kapazität und Zonentyp auf "Food"
    public FoodZone(ZoneType type, Int2D position, int capacity) {
        super(type, position, capacity);
    }


}
