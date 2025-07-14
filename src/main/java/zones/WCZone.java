package zones;

import sim.util.Int2D;

/**
 * Repräsentiert eine WC-Zone (Toilettenbereich) innerhalb der Simulation.
 * Besucher dieser Zone simulieren den Toilettengang mit Wartezeit und Kapazitätsbeschränkung.
 *
 * @author Dorothea Ziegler
 */
public class WCZone extends Zone {

    // Konstruktor setzt Position, Kapazität und Zonentypp auf "WC"

    public WCZone(ZoneType type, Int2D position, int capacity) {
        super(type, position, capacity);
    }

}
