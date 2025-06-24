package org.simulation;

import States.PanicRunState;
import sim.engine.SimState;
import sim.util.Int2D;
import org.simulation.sound.SoundType;

/**
 * A storm disturbance that affects the entire simulation area.
 * No specific position required.
 */
public class StormDisturbance extends Disturbance {

    private boolean alarmTriggered = false;

    public StormDisturbance() {
        super(null); // No position needed
    }

    @Override
    public void step(SimState state) {
        Event event = (Event) state;
        //Sturm Wanrung
        if (!alarmTriggered) {
            event.triggerStormAlert();
            alarmTriggered = true;
        }

        for (Agent agent : event.agents) {
            if (!agent.isPanicking() && !(agent.getCurrentState() instanceof PanicRunState)) {
                agent.setPanicking(true);
                agent.setCurrentState(new PanicRunState());
                System.out.println("🌪 Agent gerät wegen Sturm in Panik");
            }
        }
    }

    @Override
    public String getLabel() {
        return "Storm";
    }

    public static StormDisturbance createRandom(Event sim) {
        StormDisturbance storm = new StormDisturbance();

        int x = sim.random.nextInt(sim.grid.getWidth());
        int y = sim.random.nextInt(sim.grid.getHeight());
        sim.grid.setObjectLocation(storm, new Int2D(x, y));

        return storm;
    }
}
