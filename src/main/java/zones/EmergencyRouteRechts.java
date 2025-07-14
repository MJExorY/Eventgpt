package zones;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Int2D;

/**
 * Repräsentiert einen festen Notausgang auf der rechten Seite des Geländes.
 * Diese Klasse dient als Marker-Objekt zur Anzeige eines statischen Evakuierungspunkts.
 *
 * @author Mustafa Karakilic
 */
public class EmergencyRouteRechts implements Steppable {

    private final Int2D position;

    public EmergencyRouteRechts(Int2D position) {
        this.position = position;
    }

    public Int2D getPosition() {
        return position;
    }

    @Override
    public void step(SimState state) {
        // Keine Logik notwendig – der Ausgang ist statisch
    }

    @Override
    public String toString() {
        return "EmergencyRootRechts @ " + position;
    }
}