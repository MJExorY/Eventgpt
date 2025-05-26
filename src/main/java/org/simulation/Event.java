package org.simulation;

import sim.engine.SimState;
import sim.field.grid.SparseGrid2D;
import sim.util.Int2D;

import java.util.ArrayList;
import java.util.List;

public class Event extends SimState {
    public SparseGrid2D grid;

    private int agentCount;
    public final List<Zone> zones = new ArrayList<>();
    public final List<Agent> agents = new ArrayList<>();

    public Event(long seed) {
        super(seed);
    }

    public Event(long seed, int agentCount) {
        super(seed);
        this.agentCount = agentCount;
    }

    public static void main(String[] args) {
        Event sim = new Event(System.currentTimeMillis());
        sim.start();

        for (int i = 0; i < 10; i++) {
            sim.schedule.step(sim);
        }

        sim.finish();
        System.out.println("Event-Simulation fertig.");
    }


    @Override
    public void start() {
        super.start();

        grid = new SparseGrid2D(100, 100);

        Agent agent = new Agent();
        grid.setObjectLocation(agent, 50, 50); // In die Mitte setzen
        schedule.scheduleRepeating(agent);



        // Zonen hinzufügen
        Zone foodZone = new Zone(Zone.ZoneType.FOOD, new Int2D(10, 10), 5);
        Zone actMain = new Zone(Zone.ZoneType.ACT_MAIN, new Int2D(50, 20), 20);
        Zone actSide = new Zone(Zone.ZoneType.ACT_SIDE, new Int2D(30, 70), 15);
        Zone exitZone = new Zone(Zone.ZoneType.EXIT, new Int2D(50, 99), Integer.MAX_VALUE);

        zones.addAll(List.of(foodZone, actMain, actSide, exitZone));

        // Alle Zonen im Grid sichtbar machen
        for (Zone z : zones) {
            grid.setObjectLocation(z, z.getPosition().x, z.getPosition().y);
        }


        for (int i = 0; i < agentCount; i++) {
            Agent agentRandom = new Agent();

            int x, y;
            Int2D pos;
            do {
                x = random.nextInt(grid.getWidth());
                y = random.nextInt(grid.getHeight());
                pos = new Int2D(x, y);
            } while (getZoneByPosition(pos) != null);

            grid.setObjectLocation(agentRandom, x, y);
            schedule.scheduleRepeating(agentRandom);
        }

        //könnten bspw. Sanitäter sein
        for (int i = 0; i < 10; i++) {
            int x, y;
            Int2D pos;
            do {
                x = random.nextInt(grid.getWidth());
                y = random.nextInt(grid.getHeight());
                pos = new Int2D(x, y);
            } while (getZoneByPosition(pos) != null); // keine Zone überschreiben

            Person person = new Person(pos);
            grid.setObjectLocation(person, x, y);
            schedule.scheduleRepeating(person);
        }


        System.out.println(agentCount + " Agenten wurden erzeugt.");
        System.out.println("10 Personen wurden zusätzlich zur Simulation hinzugefügt.");
    }

    // Getter-Methode, um eine Zone nach Typ zu finden
    public Zone getZoneByType(Zone.ZoneType type) {
        return zones.stream()
                .filter(z -> z.getType() == type)
                .findFirst()
                .orElse(null);
    }

    public Zone getZoneByPosition(Int2D pos) {
        return zones.stream()
                .filter(z -> z.getPosition().equals(pos))
                .findFirst()
                .orElse(null);
    }


}
