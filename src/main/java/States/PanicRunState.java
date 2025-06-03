package States;

import org.simulation.Agent;
import org.simulation.Event;
import org.simulation.Zone;
import sim.util.Int2D;

public class PanicRunState implements IStates {
    private Int2D exitTarget = null;

    @Override
    public IStates act(Agent agent, Event event) {
        // Initialisierung (nur einmal)
        if (exitTarget == null) {
            agent.resetFlags();
            agent.setPanicking(true);

            Int2D currentPos = event.grid.getObjectLocation(agent);
            Zone nearestExit = event.getNearestAvailableExit(currentPos);
            if (nearestExit == null) {
                System.out.println("⚠ Kein verfügbarer Exit gefunden.");
                return new RoamingState(); // Kein Exit → zurück zu Roaming
            }

            exitTarget = nearestExit.getPosition();
            agent.setTargetPosition(exitTarget);
        }

        // Bewegung in Richtung Exit
        Int2D currentPos = event.grid.getObjectLocation(agent);
        int dx = Integer.compare(exitTarget.x, currentPos.x);
        int dy = Integer.compare(exitTarget.y, currentPos.y);

        int newX = Math.max(0, Math.min(event.grid.getWidth() - 1, currentPos.x + dx));
        int newY = Math.max(0, Math.min(event.grid.getHeight() - 1, currentPos.y + dy));
        Int2D newPos = new Int2D(newX, newY);

        event.grid.setObjectLocation(agent, newPos);

        // ✅ Exit erreicht?
        if (newPos.equals(exitTarget)) {
            agent.setPanicking(false);               // Panik beenden
            agent.clearTarget();                     // Ziel löschen
            return new RoamingState();               // Zustand wechseln
        }

        return this; // noch nicht angekommen → weiter im Panik-Zustand
    }

}
