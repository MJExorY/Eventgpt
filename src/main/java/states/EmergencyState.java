package states;

import org.simulation.Agent;
import org.simulation.Event;
import org.simulation.Person;
import org.simulation.utils.MovementUtils;
import sim.util.Int2D;

public class EmergencyState implements IStates {

    @Override
    public IStates act(Agent g, Event event) {
        if (!(g instanceof Person p)) return this;

        switch (p.getType()) {
            case MEDIC -> {
                Int2D target = p.getTargetPosition();

                if (target != null) {
                    MovementUtils.moveAgentTowards(p, event, target);
                    System.out.println("MEDIC bewegt sich zum Notfall bei " + target);
                } else {
                    System.out.println("MEDIC hat kein Ziel");
                    MovementUtils.randomMove(p, event);
                }
            }

            case SECURITY -> {
                Int2D target = p.getTargetPosition();

                if (target != null) {
                    MovementUtils.moveAgentTowards(p, event, target);
                    System.out.println("SECURITY bewegt sich zur Schlägerei bei " + target);
                } else {
                    System.out.println("SECURITY hat kein Ziel");
                    MovementUtils.randomMove(p, event);
                }
            }
            case VISITOR -> {
                //Notwendig aufgrund von Solar vorgabe.
            }
        }

        // Zonenbeitritt prüfen
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
}
