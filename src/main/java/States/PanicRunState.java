package States;

import org.simulation.Agent;
import org.simulation.Event;
import sim.util.Int2D;

public class PanicRunState implements IStates {

    private final Int2D exitTarget = new Int2D(0, 0); // Beispiel: Fluchtziel ist links oben
    private boolean initialized = false;

    @Override
    public IStates act(Agent agent, Event event) {
        if (!initialized) {
            agent.resetFlags();             // andere Zustände beenden
            agent.setPanicking(true);       // Fluchtmodus aktiv
            initialized = true;
        }

        Int2D currentPos = event.grid.getObjectLocation(agent);

        // Richtung berechnen (immer 1 Schritt)
        int dx = Integer.compare(exitTarget.x, currentPos.x);
        int dy = Integer.compare(exitTarget.y, currentPos.y);

        int newX = Math.max(0, Math.min(event.grid.getWidth() - 1, currentPos.x + dx));
        int newY = Math.max(0, Math.min(event.grid.getHeight() - 1, currentPos.y + dy));
        Int2D newPos = new Int2D(newX, newY);

        event.grid.setObjectLocation(agent, newPos);

        System.out.println("Agent flieht panisch nach: " + newPos);

        // Exit erreicht?
        if (newPos.equals(exitTarget)) {
            agent.setPanicking(false); // Flucht beendet
            return new RoamingState();
        }

        return this;
    }
}
