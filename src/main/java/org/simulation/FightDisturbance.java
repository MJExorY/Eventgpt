package org.simulation;

import States.EmergencyState;
import sim.engine.SimState;
import sim.util.Int2D;

import java.util.ArrayList;
import java.util.List;

/**
 * A fight disturbance at a specific position.
 * May cause aggressive reactions or draw security agents.
 */


public class FightDisturbance extends Disturbance {

    private final List<Person> assignedSecurity = new ArrayList<>();

    private final int maxSecurity = 2;


    public FightDisturbance(Int2D position) {
        super(position);
    }

    @Override
    public void step(SimState state) {
        Event event = (Event) state;

        assignedSecurity.removeIf(p -> !event.agents.contains(p));

        // Prüfe, ob bereits genug Securitys zugewiesen sind
        if (assignedSecurity.size() < maxSecurity) {
            int needed = maxSecurity - assignedSecurity.size();

            for (Agent agent : event.agents) {
                if (!(agent instanceof Person p)) continue;
                if (p.getType() != Person.PersonType.SECURITY) continue;

                // Schon einem Kampf zugewiesen?
                if (p.getTargetPosition() != null) continue;

                // Zuweisung
                p.setTargetPosition(this.position);
                p.setCurrentState(new EmergencyState());
                assignedSecurity.add(p);

                System.out.println("SECURITY permanently assigned to fight at " + position);

                needed--;
                if (needed == 0) break;
            }
        }

    }


    @Override
    public String getLabel() {
        return "Fight";
    }

    public static FightDisturbance createRandom(Event sim) {
        int x = sim.random.nextInt(sim.grid.getWidth());
        int y = sim.random.nextInt(sim.grid.getHeight());
        return new FightDisturbance(new Int2D(x, y));
    }
}
