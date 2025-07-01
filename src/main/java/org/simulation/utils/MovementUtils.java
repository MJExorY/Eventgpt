package org.simulation.utils;

import org.simulation.Agent;
import org.simulation.Event;
import org.simulation.Person;
import sim.util.Int2D;

public class MovementUtils {

    private MovementUtils() {
        throw new UnsupportedOperationException("Utility class - do not instantiate.");
    }


    /**
     * Bewegt den Agent gezielt in Richtung eines Zieles.
     * Return true, wenn Bewegung erfolgreich war.
     */

    public static boolean moveAgentTowards(Agent agent, Event event, Int2D target) {
        if (target == null) {
            return randomMove(agent, event);
        }

        Int2D currentPos = event.grid.getObjectLocation(agent);

        int dx = Integer.compare(target.x, currentPos.x);
        int dy = Integer.compare(target.y, currentPos.y);

        int newX = Math.max(0, Math.min(event.grid.getWidth() - 1, currentPos.x + dx));
        int newY = Math.max(0, Math.min(event.grid.getHeight() - 1, currentPos.y + dy));
        Int2D nextPos = new Int2D(newX, newY);

        if (!isBlocked(event, newX, newY, agent)) {
            event.grid.setObjectLocation(agent, nextPos);
            return true;
        }

        return false;
    }

    /**
     * Versucht, einen Agenten aus einer aktiven RestrictedArea herauszubewegen.
     * Return true, wenn Agent NICHT in einer RestrictedArea steht oder erfolgreich entkommen konnte.
     */
    public static boolean tryEscapeRestrictedArea(Agent agent, Event event) {
        Int2D currentPos = event.grid.getObjectLocation(agent);

        boolean inRestricted = event.getRestrictedAreas().stream()
                .anyMatch(ra -> ra.isActive() && ra.isInside(currentPos.x, currentPos.y));

        if (!inRestricted) {
            return true; // Agent steht nicht in Sperrzone
        }

        // Agent steht in Sperrzone → Raus laufen versuchen
        for (int dxTry = -1; dxTry <= 1; dxTry++) {
            for (int dyTry = -1; dyTry <= 1; dyTry++) {
                if (dxTry == 0 && dyTry == 0) continue;

                int tryX = Math.max(0, Math.min(event.grid.getWidth() - 1, currentPos.x + dxTry));
                int tryY = Math.max(0, Math.min(event.grid.getHeight() - 1, currentPos.y + dyTry));

                if (!isBlocked(event, tryX, tryY, agent)) {
                    event.grid.setObjectLocation(agent, new Int2D(tryX, tryY));
                    return true;
                }
            }
        }


        return false;
    }

    /**
     * Führt eine zufällige Bewegung durch.
     * Return true, wenn Bewegung erfolgreich war.
     */
    public static boolean randomMove(Agent agent, Event event) {
        Int2D currentPos = event.grid.getObjectLocation(agent);

        int dx = event.random.nextInt(3) - 1;
        int dy = event.random.nextInt(3) - 1;

        int newX = Math.max(0, Math.min(event.grid.getWidth() - 1, currentPos.x + dx));
        int newY = Math.max(0, Math.min(event.grid.getHeight() - 1, currentPos.y + dy));
        Int2D nextPos = new Int2D(newX, newY);

        if (!isBlocked(event, newX, newY, agent)) {
            event.grid.setObjectLocation(agent, nextPos);
            return true;
        }

        return false;
    }

    /**
     * Prüft, ob eine Position blockiert ist (z. B. durch eine aktive RestrictedArea).
     */
    public static boolean isBlocked(Event event, int x, int y, Agent agent) {
        // SECURITY und MEDIC dürfen immer in RestrictedArea laufen
        if (agent instanceof Person p &&
                (p.getType() == Person.PersonType.SECURITY ||
                        p.getType() == Person.PersonType.MEDIC)) {
            return false;
        }
        return event.getRestrictedAreas().stream()
                .anyMatch(ra -> ra.isActive() && ra.isInside(x, y));
    }


    public static boolean placeQueueAgent(Agent agent, Event event, Int2D desiredPos) {
        boolean inRestricted = event.getRestrictedAreas().stream()
                .anyMatch(ra -> ra.isActive() && ra.isInside(desiredPos.x, desiredPos.y));

        if (!inRestricted) {
            event.grid.setObjectLocation(agent, desiredPos);
            return true;
        } else {

            return false;
        }
    }
}
