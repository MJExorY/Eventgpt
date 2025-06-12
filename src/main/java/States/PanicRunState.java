package States;

import org.simulation.Agent;
import org.simulation.Event;
import org.simulation.Zone;
import sim.util.Int2D;

public class PanicRunState implements IStates {
    private boolean initialized = false;
    private Int2D target;
    private Zone exitZone;

    @Override
    public IStates act(Agent agent, Event event) {
        Int2D currentPos = event.grid.getObjectLocation(agent);

        // Einmalige Initialisierung
        if (!initialized) {
            agent.resetFlags();
            agent.setPanicking(true);

            exitZone = event.getNearestAvailableExit(currentPos);
            if (exitZone == null) {
                System.out.println("⚠ Kein Exit gefunden – zurück zu Roaming");
                return new RoamingState();
            }

            target = exitZone.getPosition();
            agent.setTargetPosition(target);
            initialized = true;
        }

        // Bewegung in Richtung Exit
        int dx = Integer.compare(target.x, currentPos.x);
        int dy = Integer.compare(target.y, currentPos.y);
        int newX = Math.max(0, Math.min(event.grid.getWidth() - 1, currentPos.x + dx));
        int newY = Math.max(0, Math.min(event.grid.getHeight() - 1, currentPos.y + dy));

        Int2D newPos = new Int2D(newX, newY);
        event.grid.setObjectLocation(agent, newPos);

        // Exit erreicht?
        if (newPos.equals(target)) {
            if (agent.tryEnterZone(exitZone)) {
                agent.clearTarget();  // Kein Ziel mehr nötig
                return this; // nicht zu Roaming wechseln – Panik bleibt aktiv!
            } else {
                // Fallback: neuer Exit wenn verfügbar
                Zone alternative = event.getNearestAvailableExit(currentPos);
                if (alternative != null && !alternative.equals(exitZone)) {
                    exitZone = alternative;
                    target = exitZone.getPosition();
                    agent.setTargetPosition(target);
                }
            }
        }

        return this;
    }
}
