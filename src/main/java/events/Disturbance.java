package events;

import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.util.Int2D;

/**
 * Abstrakte Basisklasse für alle Arten von Störungen im Event-Szenario (z. B. Feuer, Kampf, Sturm).
 * Definiert gemeinsame Eigenschaften wie Position und Label, sowie einen Stopp-Mechanismus.
 *
 * @author Lukas Kilian
 */
public abstract class Disturbance implements Steppable {
    protected Int2D position;


    public Disturbance(Int2D position) {
        this.position = position;
    }

    /**
     * Gibt die Position der Störung im Simulationsraster zurück.
     *
     * @return position
     * @author Lukas Kilian
     */
    public Int2D getPosition() {
        return position;
    }

    /**
     * Gibt eine textuelle Bezeichnung der Störung zurück (z. B. "Feuer", "Kampf").
     * Muss von Unterklassen implementiert werden.
     *
     * @return String
     * @author Lukas Kilian
     */
    public abstract String getLabel(); // e.g., "Fire", "Fight", etc.

    protected Stoppable stopper;

    public void setStopper(Stoppable stopper) {
        this.stopper = stopper;
    }
}
