package org.simulation;

import sim.engine.SimState;
import sim.engine.Stoppable;
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
        Event sim = new Event(System.currentTimeMillis(), 15);
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
        agent.setEvent(this);
        agents.add(agent);
        grid.setObjectLocation(agent, 50, 50);
        Stoppable stopper = schedule.scheduleRepeating(agent);
        agent.setStopper(stopper);


        // Zonen hinzufügen
        Zone foodZone = new Zone(Zone.ZoneType.FOOD, new Int2D(5, 15), 5);          // Links oben
        Zone wcZone = new Zone(Zone.ZoneType.WC, new Int2D(90, 25), 10);             // Rechts oben
        Zone actMain = new Zone(Zone.ZoneType.ACT_MAIN, new Int2D(50, 45), 20);     // Zentrum
        Zone actSide = new Zone(Zone.ZoneType.ACT_SIDE, new Int2D(15, 85), 15);     // Links unten
        Zone exitZone = new Zone(Zone.ZoneType.EXIT, new Int2D(60, 90), Integer.MAX_VALUE); // Unten Mitte

        zones.addAll(List.of(foodZone, wcZone, actMain, actSide, exitZone));


        // Alle Zonen im Grid sichtbar machen
        for (Zone z : zones) {
            grid.setObjectLocation(z, z.getPosition().x, z.getPosition().y);
        }


        for (int i = 0; i < agentCount; i++) {
            Agent agentRandom = new Agent();
            agentRandom.setEvent(this);    // Event Referenz setzen
            agents.add(agentRandom);       // Agent in Liste hinzufügen

            int x, y;
            Int2D pos;
            do {
                x = random.nextInt(grid.getWidth());
                y = random.nextInt(grid.getHeight());
                pos = new Int2D(x, y);
            } while (getZoneByPosition(pos) != null);

            grid.setObjectLocation(agentRandom, x, y);
            agentRandom.setStopper(schedule.scheduleRepeating(agentRandom));
        }

        // Sanitäter hinzufügen (5 Personen)
        for (int i = 0; i < 5; i++) {
            int x, y;
            Int2D pos;
            do {
                x = random.nextInt(grid.getWidth());
                y = random.nextInt(grid.getHeight());
                pos = new Int2D(x, y);
            } while (getZoneByPosition(pos) != null); // keine Zone überschreiben

            Person medic = new Person(pos, Person.PersonType.MEDIC);
            grid.setObjectLocation(medic, x, y);
            schedule.scheduleRepeating(medic);
        }

        // Security hinzufügen (5 Personen)
        for (int i = 0; i < 5; i++) {
            int x, y;
            Int2D pos;
            do {
                x = random.nextInt(grid.getWidth());
                y = random.nextInt(grid.getHeight());
                pos = new Int2D(x, y);
            } while (getZoneByPosition(pos) != null);

            Person security = new Person(pos, Person.PersonType.SECURITY);
            grid.setObjectLocation(security, x, y);
            schedule.scheduleRepeating(security);
        }

        System.out.println(agentCount + " Agenten wurden erzeugt.");
        System.out.println("5 Sanitäter und 5 Security-Personen wurden zur Simulation hinzugefügt.");
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

    public void spawn(Disturbance disturbance) {
        if (disturbance.getPosition() != null) {
            grid.setObjectLocation(disturbance, disturbance.getPosition().x, disturbance.getPosition().y);
        }
        schedule.scheduleRepeating(disturbance);
    }

    public Zone getNearestAvailableExit(Int2D fromPosition) {
        return zones.stream()
                .filter(z -> z.getType() == Zone.ZoneType.EXIT && !z.isFull())
                .min((z1, z2) -> {
                    int d1 = Math.abs(z1.getPosition().x - fromPosition.x) + Math.abs(z1.getPosition().y - fromPosition.y);
                    int d2 = Math.abs(z2.getPosition().x - fromPosition.x) + Math.abs(z2.getPosition().y - fromPosition.y);
                    return Integer.compare(d1, d2);
                })
                .orElse(null);
    }


}
