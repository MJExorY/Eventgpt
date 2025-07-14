package events;

import org.simulation.Agent;
import org.simulation.Event;
import states.PanicRunState;
import sim.engine.SimState;
import sim.util.Int2D;

/**
 * Repräsentiert eine Sturm-Störung (StormDisturbance), die global auf alle Agenten wirkt.
 * Es wird kein konkreter Ort benötigt – alle Agenten geraten in Panik.
 * Bei Auslösung wird einmalig ein Sturmwarnungs-Alarm getriggert.
 *
 * @author Lukas Kilian
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
