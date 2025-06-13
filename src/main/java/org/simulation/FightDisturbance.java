package org.simulation;

import States.EmergencyState;
import sim.engine.SimState;
import sim.util.Int2D;

/**
 * A fight disturbance at a specific position.
 * May cause aggressive reactions or draw security agents.
 */
public class FightDisturbance extends Disturbance {

    public FightDisturbance(Int2D position) {
        super(position);
    }

    @Override
    public void step(SimState state) {
        Event event = (Event) state;

        for (Agent agent : event.agents) {
            Int2D agentPos = event.grid.getObjectLocation(agent);
            if (agentPos != null && position != null) {
                double distance = agentPos.distance(position);

                if (distance <= 10) {
                    agent.setCurrentState(new EmergencyState());

                    if (agent instanceof Person p && p.getType() == Person.PersonType.SECURITY) {
                        p.setTargetPosition(this.position);
                    }
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
