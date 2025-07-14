package events;

import org.simulation.Agent;
import org.simulation.Event;
import org.simulation.Person;
import states.EmergencyState;
import states.PanicRunState;
import sim.engine.SimState;
import sim.util.Int2D;
import org.simulation.utils.MovementUtils;
import org.simulation.RestrictedArea;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Modelliert eine Kampf-Störung auf dem Eventgelände.
 * Verantwortlich für das Einrichten eines RestrictedArea, Zuweisung von Security und Sanitätern,
 * Auslösen von Panik bei umliegenden Agenten und Auflösen der Störung.
 *
 * @author Lukas Kilian
 */

public class FightDisturbance extends Disturbance {


    private final List<Person> assignedSecurity = new ArrayList<>();
    private boolean resolved = false;
    private Person assignedMedic = null;
    private int ticksSinceResolved = -1;
    private static final Logger logger = Logger.getLogger(FightDisturbance.class.getName());


    public FightDisturbance(Int2D position) {
        super(position);
    }

    /**
     * Haupt-Logik zur Verarbeitung der Störung in jedem Zeitschritt.
     *
     * @param state
     * @author Dorothea Ziegler
     */
    @Override
    public void step(SimState state) {
        Event event = (Event) state;
        Int2D loc = event.grid.getObjectLocation(this);

        if (resolved) {
            deactivateRestrictedAreaIfNeeded(event, loc);
            event.grid.remove(this);
            return;
        }

        if (shouldCreateRestrictedArea(event, loc)) {
            createRestrictedAreaAndDistributeSecurity(event, loc);
        }

        handleResolution(event);

        assignedSecurity.removeIf(p -> !event.agents.contains(p));

        if (assignedSecurity.isEmpty()) {
            assignSecurityAndMedic(event);
        }

        if (checkIfFightResolved(event)) return;

        panicNearbyAgents(event);
    }


    private void handleResolvedState(Event event) {
        ticksSinceResolved++;
        if (ticksSinceResolved >= 3) {
            event.grid.remove(this);
            if (stopper != null) stopper.stop();
            if (logger.isLoggable(Level.INFO)) {
                logger.info(String.format("🕒 Fight-Icon entfernt nach 3 Ticks bei %s", position));
            }
        }
    }

    private void assignSecurityAndMedic(Event event) {
        for (Agent agent : event.agents) {
            if (agent instanceof Person p &&
                    p.getType() == Person.PersonType.SECURITY &&
                    p.getTargetPosition() == this.position) {

                p.setTargetPosition(this.position);
                p.setCurrentState(new EmergencyState());
                assignedSecurity.add(p);

                if (logger.isLoggable(Level.INFO)) {
                    logger.info(String.format("👮 SECURITY permanently assigned to fight at %s", position));
                }
                break;
            }
        }

        for (Agent agent : event.agents) {
            if (agent instanceof Person p &&
                    p.getType() == Person.PersonType.MEDIC &&
                    p.getTargetPosition() == null) {

                p.setTargetPosition(this.position);
                p.setCurrentState(new EmergencyState());
                assignedMedic = p;

                if (logger.isLoggable(Level.INFO)) {
                    logger.info(String.format("🚑 MEDIC assigned to support at %s", position));
                }
                break;
            }
        }
    }

    private boolean checkIfFightResolved(Event event) {
        if (securityPresent(event) && medicPresent(event)) {
            resolveFight(event);
            return true;
        }
        return false;
    }


    private void panicNearbyAgents(Event event) {
        for (Agent agent : event.agents) {
            if (agent instanceof Person) continue; // Sanitäter & Security ignorieren

            Int2D agentPos = event.grid.getObjectLocation(agent);
            if (agentPos != null && position != null) {
                int dx = agentPos.x - position.x;
                int dy = agentPos.y - position.y;
                double distance = Math.sqrt((double) dx * dx + dy * dy);

                if (distance <= 2 && !agent.isPanicking()) {
                    agent.setPanicking(true);
                    agent.setCurrentState(new PanicRunState());

                    if (logger.isLoggable(Level.INFO)) {
                        logger.info(String.format("😱 Agent gerät wegen Fight in Panik bei %s", agentPos));
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

    public boolean isResolved() {
        return resolved;
    }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }

    public List<Person> getAssignedSecurity() {
        return assignedSecurity;
    }

    public Person getAssignedMedic() {
        return assignedMedic;
    }

    public void setAssignedMedic(Person assignedMedic) {
        this.assignedMedic = assignedMedic;
    }

    private void deactivateRestrictedAreaIfNeeded(Event event, Int2D loc) {
        if (loc == null) return;

        for (RestrictedArea ra : event.getRestrictedAreas()) {
            if (ra.getCenterX() == loc.x && ra.getCenterY() == loc.y) {
                ra.deactivate();
            }
        }
    }

    private boolean shouldCreateRestrictedArea(Event event, Int2D loc) {
        return loc != null &&
                event.getRestrictedAreas().stream().noneMatch(ra ->
                        ra.getCenterX() == loc.x && ra.getCenterY() == loc.y);
    }

    private void createRestrictedAreaAndDistributeSecurity(Event event, Int2D loc) {
        RestrictedArea fightZone = new RestrictedArea(loc.x, loc.y, 8);
        event.addRestrictedArea(fightZone);
        event.grid.setObjectLocation(fightZone, loc);

        long securityCount = event.agents.stream()
                .filter(a -> a instanceof Person p && p.getType() == Person.PersonType.SECURITY)
                .count();

        int i = 0;
        for (Agent agent : event.agents) {
            if (agent instanceof Person p && p.getType() == Person.PersonType.SECURITY) {
                if (i == 0) {
                    assignSecurityToCenter(p);
                } else {
                    placeSecurityAtPerimeter(event, fightZone, p, i, securityCount);
                }
                i++;
            }
        }


    }

    private void assignSecurityToCenter(Person p) {
        p.setTargetPosition(this.position);
        p.setCurrentState(new EmergencyState());
        if (logger.isLoggable(Level.INFO)) {
            logger.info(String.format("👮 Security #1 läuft ins Zentrum des Kampfes: %s", this.position));
        }
    }

    private void placeSecurityAtPerimeter(Event event, RestrictedArea fightZone, Person p, int i, long securityCount) {
        double angle = 2 * Math.PI * i / Math.max(securityCount - 1, 1);
        int x = (int) (fightZone.getCenterX() + fightZone.getRadius() * Math.cos(angle));
        int y = (int) (fightZone.getCenterY() + fightZone.getRadius() * Math.sin(angle));
        Int2D ringPos = new Int2D(x, y);

        if (!MovementUtils.isBlocked(event, x, y, p)) {
            p.setTargetPosition(ringPos);
            p.setCurrentState(new EmergencyState());
            if (logger.isLoggable(Level.INFO)) {
                logger.info(String.format("👮 Security #%d stellt sich auf Abriegelungsposition: %s", (i + 1), ringPos));
            }
        } else {
            if (logger.isLoggable(Level.INFO)) {
                logger.info(String.format("⚠ Abriegelungsposition blockiert für Security #%d: %s", (i + 1), ringPos));
            }
        }
    }

    private void handleResolution(Event event) {
        if (resolved) {
            handleResolvedState(event);
        }
    }

    private boolean securityPresent(Event event) {
        return assignedSecurity.stream()
                .map(event.grid::getObjectLocation)
                .anyMatch(pos -> pos != null && pos.equals(this.position));
    }

    private boolean medicPresent(Event event) {
        if (assignedMedic == null) return false;
        Int2D medicPos = event.grid.getObjectLocation(assignedMedic);
        return medicPos != null && medicPos.equals(this.position);
    }

    void resolveFight(Event event) {
        resolved = true;

        releasePerimeterSecurity(event);
        releaseAssignedSecurity();
        releaseAssignedMedic();

        if (logger.isLoggable(Level.INFO)) {
            logger.info(String.format("✅ FightDisturbance beendet durch Security und Medic bei %s", position));
        }
    }

    private void releasePerimeterSecurity(Event event) {
        for (Agent agent : event.agents) {
            if (agent instanceof Person p
                    && p.getType() == Person.PersonType.SECURITY
                    && p.getTargetPosition() != null
                    && !p.getTargetPosition().equals(this.position)) {

                p.clearTarget();
                p.setCurrentState(new EmergencyState());
                if (logger.isLoggable(Level.INFO)) {
                    logger.info("SECURITY verlässt den Fight-Perimeter und kehrt zu Roaming zurück.");
                }
            }
        }
    }

    private void releaseAssignedSecurity() {
        for (Person sec : assignedSecurity) {
            sec.clearTarget();
            sec.setCurrentState(new EmergencyState());
        }
        assignedSecurity.clear();
    }

    private void releaseAssignedMedic() {
        if (assignedMedic != null) {
            assignedMedic.clearTarget();
            assignedMedic.setCurrentState(new EmergencyState());
            assignedMedic = null;
        }
    }


}
