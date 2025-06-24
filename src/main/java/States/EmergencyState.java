package States;

import org.simulation.Agent;
import org.simulation.Event;
import org.simulation.Person;
import sim.util.Int2D;

public class EmergencyState implements IStates {

    @Override
    public IStates act(Agent g, Event event) {
        if (!(g instanceof Person p)) return this;

        switch (p.getType()) {
            case MEDIC -> {
                Int2D target = p.getTargetPosition();

                if (target != null) {
                    moveTowards(p, target, event);
                    System.out.println("MEDIC bewegt sich zum Notfall bei " + target);
                } else {
                    System.out.println("MEDIC hat kein Ziel");
                    randomStep(p, event);
                }
            }

            case SECURITY -> {
                Int2D target = p.getTargetPosition();

                if (target != null) {
                    moveTowards(p, target, event);
                    System.out.println("SECURITY bewegt sich zur Schlägerei bei " + target);
                } else {
                    System.out.println("SECURITY hat kein Ziel");
                    randomStep(p, event);
                }
            }

        }

        Int2D pos = event.grid.getObjectLocation(g);
        var zone = event.getZoneByPosition(pos);
        if (zone != null) {
            boolean success = g.tryEnterZone(zone);
            if (success) {
                System.out.println("Agent ist der Zone " + zone.getType() + " beigetreten.");
            }
        }
        return this;
    }

    private void moveTowards(Agent a, Int2D target, Event event) {
        Int2D current = event.grid.getObjectLocation(a);
        int dx = Integer.compare(target.x, current.x);
        int dy = Integer.compare(target.y, current.y);
        Int2D next = new Int2D(current.x + dx, current.y + dy);

        // einfache Begrenzung (optional)
        int maxX = event.grid.getWidth() - 1;
        int maxY = event.grid.getHeight() - 1;
        int newX = Math.max(0, Math.min(next.x, maxX));
        int newY = Math.max(0, Math.min(next.y, maxY));

        event.grid.setObjectLocation(a, new Int2D(newX, newY));
    }

    private void randomStep(Agent a, Event event) {
        Int2D pos = event.grid.getObjectLocation(a);
        int dx = event.random.nextInt(3) - 1;
        int dy = event.random.nextInt(3) - 1;
        int newX = Math.max(0, Math.min(event.grid.getWidth() - 1, pos.x + dx));
        int newY = Math.max(0, Math.min(event.grid.getHeight() - 1, pos.y + dy));
        event.grid.setObjectLocation(a, new Int2D(newX, newY));
    }
}
