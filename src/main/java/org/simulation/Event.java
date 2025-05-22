package org.simulation;

import sim.engine.SimState;
import sim.field.grid.SparseGrid2D;
import sim.util.Int2D;

public class Event extends SimState {
    public SparseGrid2D grid;

    public Event(long seed) {
        super(seed);
    }


    //----

    private int agentCount;

    public Event(long seed, int agentCount) {
        super(seed);
        this.agentCount = agentCount;
    }






    //---


    @Override
    public void start() {
        super.start();
        grid = new SparseGrid2D(100, 100);

        Agent agent = new Agent();
        grid.setObjectLocation(agent, 50, 50); // In die Mitte setzen
        schedule.scheduleRepeating(agent);


//Methode um die Anzahl dynamisch zur Lufzeit zu bestimmen
        super.start();
        grid = new SparseGrid2D(100, 100);

        for (int i = 0; i < agentCount; i++) {
            Agent agentRandom = new Agent();

            // Zufällige Position im Grid
            int x = random.nextInt(grid.getWidth());
            int y = random.nextInt(grid.getHeight());

            // Optional: Stelle sicher, dass kein anderer Agent schon dort ist
            while (grid.getObjectsAtLocation(x, y) != null) {
                x = random.nextInt(grid.getWidth());
                y = random.nextInt(grid.getHeight());
            }

            grid.setObjectLocation(agentRandom, x, y);
            schedule.scheduleRepeating(agentRandom);
        }

        //könnten bspw. Sanitäter sein
        for (int i = 0; i < 10; i++) {
            int x = random.nextInt(grid.getWidth());
            int y = random.nextInt(grid.getHeight());

            // Stelle sicher, dass die Position frei ist
            while (grid.getObjectsAtLocation(x, y) != null) {
                x = random.nextInt(grid.getWidth());
                y = random.nextInt(grid.getHeight());
            }

            Person person = new Person(new Int2D(x, y));
            grid.setObjectLocation(person, x, y);
            schedule.scheduleRepeating(person);
        }

        System.out.println(agentCount + " Agenten wurden erzeugt.");
        System.out.println("10 Personen wurden zusätzlich zur Simulation hinzugefügt.");
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
}
