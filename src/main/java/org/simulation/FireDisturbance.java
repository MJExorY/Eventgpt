package org.simulation;

import States.PanicRunState;
import sim.engine.SimState;
import sim.util.Int2D;
import org.simulation.sound.SoundType;

public class FireDisturbance extends Disturbance {

    private boolean alarmTriggered = false;

    public FireDisturbance(Int2D position) {
        super(position);
    }

    @Override
    public void step(SimState state) {
        Event event = (Event) state;

        //Feueralarm
        if (!alarmTriggered) {
            event.triggerFireAlarm(position);
            alarmTriggered = true;
        }

        for (Agent agent : event.agents) {
            Int2D agentPos = event.grid.getObjectLocation(agent);
            if (agentPos != null && position != null) {
                int dx = agentPos.x - position.x;
                int dy = agentPos.y - position.y;
                double distance = Math.sqrt(dx * dx + dy * dy);

                if (distance <= 10) { // Radius frei wählbar
                    agent.setPanicking(true);
                    agent.setCurrentState(new PanicRunState());
                }
            }
        }
    }

    @Override
    public String getLabel() {
        return "Fire";
    }

    public static FireDisturbance createRandom(Event sim) {
        int x = sim.random.nextInt(sim.grid.getWidth());
        int y = sim.random.nextInt(sim.grid.getHeight());
        return new FireDisturbance(new Int2D(x, y));
    }
}
