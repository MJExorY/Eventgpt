package events;

import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.util.Int2D;

/**
 * Abstract base class for all types of disturbances (e.g., fire, fight, storm).
 * Defines a position and a label. Subclasses implement specific behavior.
 */
public abstract class Disturbance implements Steppable {
    protected Int2D position;

    public Disturbance(Int2D position) {
        this.position = position;
    }

    public Int2D getPosition() {
        return position;
    }

    public abstract String getLabel(); // e.g., "Fire", "Fight", etc.

    protected Stoppable stopper;

    public void setStopper(Stoppable stopper) {
        this.stopper = stopper;
    }
}
