package org.simulation;

import states.EmergencyState;
import states.RoamingState;
import states.IStates;
import sim.engine.SimState;
import sim.util.Int2D;
import org.simulation.utils.MovementUtils;
import zones.Zone;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.Color;

/**
 * Repräsentiert eine Person in der Simulation – kann ein Besucher, Sanitäter oder Sicherheitskraft sein.
 * Das Verhalten wird durch unterschiedliche Zustände gesteuert.
 *
 * @author Betuel
 */

public class Person extends Agent {
    private static final Logger logger = Logger.getLogger(Person.class.getName());

    public enum PersonType {
        MEDIC, SECURITY, VISITOR
    }

    private final PersonType type;

    public Person(PersonType type) {
        this.type = type;
        initStateForType();  // Zustand je nach Rolle setzen
    }

    public PersonType getType() {
        return type;
    }

    public void initStateForType() {
        switch (type) {
            case MEDIC -> setCurrentState(new EmergencyState());
            case SECURITY -> setCurrentState(new EmergencyState());
            case VISITOR -> setCurrentState(new RoamingState());
        }
    }

    @Override
    public Color getColor() {
        return switch (type) {
            case MEDIC -> Color.WHITE;
            case SECURITY -> Color.DARK_GRAY;
            case VISITOR -> Color.yellow;
        };
    }


    @Override
    public void step(SimState state) {
        Event sim = (Event) state;

        runStateMachine(sim);
        if (!attemptEscape(sim)) return;

        moveAgent(sim);
        if (checkExitAndRemove(sim)) return;

        logAgentStatus(sim);
    }

    private void runStateMachine(Event sim) {
        if (getCurrentState() != null) {
            IStates nextState = getCurrentState().act(this, sim);
            setCurrentState(nextState);
        }
    }

    private boolean attemptEscape(Event sim) {
        return MovementUtils.tryEscapeRestrictedArea(this, sim);
    }

    private void moveAgent(Event sim) {
        if (getTargetPosition() != null) {
            boolean moved = MovementUtils.moveAgentTowards(this, sim, getTargetPosition());
            logIf(!moved, "🚫 Person blockiert auf dem Weg zu Ziel: %s", getTargetPosition());
        } else {
            MovementUtils.randomMove(this, sim);
        }
    }

    private boolean checkExitAndRemove(Event sim) {
        Int2D pos = sim.grid.getObjectLocation(this);
        if (pos == null) return false;

        Zone currentZone = sim.getZoneByPosition(pos);
        if (currentZone != null &&
                (currentZone.getType() == Zone.ZoneType.EXIT ||
                        (currentZone.getType() == Zone.ZoneType.EMERGENCY_EXIT && isPanicking()))) {

            logIf(true, "👋 Person erreicht Ausgang und wird entfernt: %s", pos);

            if (getStopper() != null) getStopper().stop();
            sim.grid.remove(this);
            sim.agents.remove(this);
            return true;
        }
        return false;
    }

    private void logAgentStatus(Event sim) {
        Int2D pos = sim.grid.getObjectLocation(this);
        logIf(true,
                "👤 Person @ %s | Type: %s | State: %s | Target: %s",
                pos,
                type,
                (getCurrentState() != null ? getCurrentState().getClass().getSimpleName() : "null"),
                getTargetPosition());
    }

    private void logIf(boolean condition, String format, Object... params) {
        if (condition && logger.isLoggable(Level.INFO)) {
            logger.info(String.format(format, params));
        }
    }


}

