package zones;

import org.simulation.Agent;
import sim.util.Int2D;

import java.util.HashSet;
import java.util.Set;

/**
 * Repräsentiert eine generische Zone innerhalb des Simulationsgeländes.
 * Zonen haben einen bestimmten Typ (z.B. WC, FOOD), eine feste Position und eine Kapazitätsgrenze.
 * Agenten können diese Zonen betreten und verlassen.
 *
 * @author cb-235866
 */
public class Zone {


    public enum ZoneType {
        FOOD, WC, ACT_MAIN, ACT_SIDE, EXIT, EMERGENCY_EXIT, EMERGENCYROOTRIGHT
    }

    public final ZoneType type;
    private final Int2D position;
    private final int capacity;

    private final Set<Agent> currentOccupants = new HashSet<>();

    public Zone(ZoneType type, Int2D position, int capacity) {
        this.type = type;
        this.position = position;
        this.capacity = capacity;
    }

    public ZoneType getType() {
        return type;
    }

    public Int2D getPosition() {
        return position;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getOccupancy() {
        return currentOccupants.size();
    }

    public boolean isFull() {
        return currentOccupants.size() >= capacity;
    }

    public void enter(Agent agent) {
        currentOccupants.add(agent);
    }

    public void leave(Agent agent) {
        currentOccupants.remove(agent);
    }

    public boolean contains(Agent agent) {
        return currentOccupants.contains(agent);
    }
}
