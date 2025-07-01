package events;

import org.simulation.Agent;
import org.simulation.Event;
import org.simulation.Person;
import org.simulation.RestrictedArea;
import org.simulation.utils.MovementUtils;
import sim.engine.SimState;
import sim.util.Int2D;
import states.EmergencyState;
import states.PanicRunState;
import states.RoamingState;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FireDisturbance extends Disturbance {

    private boolean alarmTriggered = false;
    private boolean resolved = false;
    private final int panicRadius = 10;
    private static final Logger logger = Logger.getLogger(FireDisturbance.class.getName());

    public Int2D getPosition() {
        return position;
    }

    public int getPanicRadius() {
        return panicRadius;
    }


    public FireDisturbance(Int2D position) {
        super(position);
    }

    @Override
    public void step(SimState state) {
        Event event = (Event) state;
        Int2D loc = event.grid.getObjectLocation(this);

        if (resolved) {
            handleResolution(event, loc);
            return;
        }

        if (shouldCreateRestrictedArea(event, loc)) {
            createRestrictedAreaAndDistributeSecurity(event, loc);
        }

        triggerAlarmIfNeeded(event);

        panicNearbyAgents(event);
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

    public void resolve(Event event) {
        this.resolved = true;

        // 🔴 Sperrzone sofort deaktivieren
        for (RestrictedArea ra : event.getRestrictedAreas()) {
            if (ra.getCenterX() == position.x && ra.getCenterY() == position.y) {
                ra.deactivate();
                logger.info("🔴 Sperrzone deaktiviert (direkt in resolve())");
            }
        }

        // Security freigeben
        for (Agent agent : event.agents) {
            if (agent instanceof Person p
                    && p.getType() == Person.PersonType.SECURITY
                    && p.getCurrentState() instanceof states.EmergencyState) {

                p.clearTarget();
                p.setCurrentState(new RoamingState());
                logger.info("🔄 SECURITY verlässt Feuer-Einsatz und kehrt zu Roaming zurück.");
            }
        }

        // FireDisturbance sofort entfernen
        if (stopper != null) stopper.stop();
        event.grid.remove(this);
    }

    private void handleResolution(Event event, Int2D loc) {
        if (loc != null) {
            for (RestrictedArea ra : event.getRestrictedAreas()) {
                if (ra.getCenterX() == position.x && ra.getCenterY() == position.y) {
                    ra.deactivate();
                    logger.info("🔴 Sperrzone deaktiviert");
                }
            }
        }

        if (stopper != null) stopper.stop();
        event.grid.remove(this);
    }

    private boolean shouldCreateRestrictedArea(Event event, Int2D loc) {
        return loc != null &&
                event.getRestrictedAreas().stream()
                        .noneMatch(ra -> ra.getCenterX() == loc.x && ra.getCenterY() == loc.y);
    }

    private void createRestrictedAreaAndDistributeSecurity(Event event, Int2D loc) {
        RestrictedArea fireZone = new RestrictedArea(loc.x, loc.y, panicRadius);
        event.addRestrictedArea(fireZone);
        event.grid.setObjectLocation(fireZone, loc);

        List<Person> securityAgents = event.agents.stream()
                .filter(a -> a instanceof Person p && p.getType() == Person.PersonType.SECURITY)
                .map(a -> (Person) a)
                .toList();

        int total = securityAgents.size();

        for (int i = 0; i < total; i++) {
            Person p = securityAgents.get(i);
            Int2D newPos = computeSecurityPosition(fireZone, i, total);
            assignSecurityPosition(event, p, newPos);
        }
    }

    private void assignSecurityPosition(Event event, Person p, Int2D newPos) {
        boolean blocked = MovementUtils.isBlocked(event, newPos.x, newPos.y, p);

        if (!blocked) {
            p.setTargetPosition(newPos);
            p.setCurrentState(new EmergencyState());
            logInfo("SECURITY positioniert sich bei %s", newPos);
        } else {
            logInfo("Ziel für SECURITY blockiert durch Sperrzone: %s", newPos);
        }
    }

    private void logInfo(String message, Object param) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info(String.format(message, param));
        }
    }


    private Int2D computeSecurityPosition(RestrictedArea fireZone, int index, long securityCount) {
        double angle = 2 * Math.PI * index / Math.max(securityCount, 1);
        int x = (int) (fireZone.getCenterX() + fireZone.getRadius() * Math.cos(angle));
        int y = (int) (fireZone.getCenterY() + fireZone.getRadius() * Math.sin(angle));
        return new Int2D(x, y);
    }

    private void triggerAlarmIfNeeded(Event event) {
        if (!alarmTriggered) {
            event.triggerFireAlarm(position);
            alarmTriggered = true;
        }
    }

    private void panicNearbyAgents(Event event) {
        for (Agent agent : event.agents) {
            Int2D agentPos = event.grid.getObjectLocation(agent);
            if (agentPos != null && position != null) {
                double distance = agentPos.distance(position);

                if (distance <= panicRadius &&
                        agent instanceof Person p &&
                        p.getType() == Person.PersonType.VISITOR) {
                    agent.setPanicking(true);
                    agent.setCurrentState(new PanicRunState());
                    if (logger.isLoggable(Level.INFO)) {
                        logger.info(String.format("😱 Visitor gerät in Panik bei %s", agentPos));
                    }
                }
            }
        }
    }


}
