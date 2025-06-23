package org.simulation;

import States.EmergencyState;
import States.PanicRunState;
import sim.engine.SimState;
import sim.util.Int2D;

import java.util.ArrayList;
import java.util.List;

public class FightDisturbance extends Disturbance {

    private final List<Person> assignedSecurity = new ArrayList<>();

    public FightDisturbance(Int2D position) {
        super(position);
    }

    @Override
    public void step(SimState state) {
        Event event = (Event) state;

        // Entferne Security, die nicht mehr im Spiel sind
        assignedSecurity.removeIf(p -> !event.agents.contains(p));

        // Security hinzuziehen, falls noch keiner da ist
        if (assignedSecurity.isEmpty()) {
            for (Agent agent : event.agents) {
                if (!(agent instanceof Person p)) continue;
                if (p.getType() != Person.PersonType.SECURITY) continue;
                if (p.getTargetPosition() != null) continue;

                p.setTargetPosition(this.position);
                p.setCurrentState(new EmergencyState());
                assignedSecurity.add(p);

                System.out.println("👮 SECURITY permanently assigned to fight at " + position);
                break;
            }
        }

        // NEU: Besucher in der Nähe geraten in Panik
        for (Agent agent : event.agents) {
            if (agent instanceof Person) continue; // Sanitäter & Security ignorieren

            Int2D agentPos = event.grid.getObjectLocation(agent);
            if (agentPos != null && position != null) {
                int dx = agentPos.x - position.x;
                int dy = agentPos.y - position.y;
                double distance = Math.sqrt(dx * dx + dy * dy);

                if (distance <= 2 && !agent.isPanicking()) {
                    agent.setPanicking(true);
                    agent.setCurrentState(new PanicRunState());
                    System.out.println("Agent gerät wegen Fight in Panik bei " + agentPos);
                }
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
