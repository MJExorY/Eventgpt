package org.simulation;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.field.grid.SparseGrid2D;
import sim.util.Int2D;
import org.simulation.sound.EventSoundSystem;
import org.simulation.sound.SoundType;
import org.simulation.FireStation;
import org.simulation.FireTruck;

import java.util.ArrayList;
import java.util.List;

public class Event extends SimState {

    private int visitorCount;
    private int medicCount;
    private int securityCount;
    public SparseGrid2D grid;
    public final List<Zone> zones = new ArrayList<>();
    public final List<Agent> agents = new ArrayList<>();
    private EventSoundSystem soundSystem;
    private FireStation fireStation;

    public Event(long seed) {
        super(seed);
    }

    public Event(long seed, int visitorCount, int medicCount, int securityCount) {
        super(seed);
        this.visitorCount = visitorCount;
        this.medicCount = medicCount;
        this.securityCount = securityCount;
        this.soundSystem = new EventSoundSystem();

    }


    public static void main(String[] args) {
        Event sim = new Event(System.currentTimeMillis(), 15, 15, 15);
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

        /*Agent agent = new Agent();
        agent.setEvent(this);
        agents.add(agent);
        grid.setObjectLocation(agent, 50, 50);
        Stoppable stopper = schedule.scheduleRepeating(agent);
        agent.setStopper(stopper); */


        // Zonen hinzufügen
        Zone foodZone = new Zone(Zone.ZoneType.FOOD, new Int2D(5, 15), 5);          // Links oben
        Zone wcZone = new Zone(Zone.ZoneType.WC, new Int2D(90, 25), 10);             // Rechts oben
        Zone actMain = new Zone(Zone.ZoneType.ACT_MAIN, new Int2D(50, 45), 20);     // Zentrum
        Zone actSide = new Zone(Zone.ZoneType.ACT_SIDE, new Int2D(15, 85), 15);     // Links unten

        Zone normalExit = new Zone(Zone.ZoneType.EXIT, new Int2D(60, 90), Integer.MAX_VALUE); // Unten Mitte

        Zone emergencyNorth = new Zone(Zone.ZoneType.EMERGENCY_EXIT, new Int2D(50, 5), Integer.MAX_VALUE);    // Norden
        Zone emergencySouth = new Zone(Zone.ZoneType.EMERGENCY_EXIT, new Int2D(30, 95), Integer.MAX_VALUE);   // Süden (links vom normalen Exit)
        Zone emergencyEast = new Zone(Zone.ZoneType.EMERGENCY_EXIT, new Int2D(95, 50), Integer.MAX_VALUE);    // Osten
        Zone emergencyWest = new Zone(Zone.ZoneType.EMERGENCY_EXIT, new Int2D(5, 50), Integer.MAX_VALUE);     // Westen
        Zone emergencyNorthEast = new Zone(Zone.ZoneType.EMERGENCY_EXIT, new Int2D(85, 15), Integer.MAX_VALUE); // Nordosten
        Zone emergencySouthWest = new Zone(Zone.ZoneType.EMERGENCY_EXIT, new Int2D(95, 95), Integer.MAX_VALUE); // Südost

        zones.addAll(List.of(foodZone, wcZone, actMain, actSide, normalExit,
                emergencyNorth, emergencySouth, emergencyEast, emergencyWest, emergencyNorthEast, emergencySouthWest));


        // Alle Zonen im Grid sichtbar machen
        for (Zone z : zones) {
            grid.setObjectLocation(z, z.getPosition().x, z.getPosition().y);
        }

        // Feuerwache erstellen
        fireStation = new FireStation(new Int2D(95, 70), this);
        grid.setObjectLocation(fireStation, fireStation.getPosition().x, fireStation.getPosition().y);
        System.out.println("Feuerwache wurde bei " + fireStation.getPosition() + " erstellt");
        Int2D eingang = new Int2D(60, 90); // Eingang Zone

        for (int i = 0; i < visitorCount; i++) {
            final int delay = i;
            final int index = i;


            schedule.scheduleOnce(delay, new Steppable() {
                @Override
                public void step(SimState state) {
                    Agent agent = new Agent();
                    agent.setEvent((Event) state);
                    ((Event) state).agents.add(agent);
                    ((Event) state).grid.setObjectLocation(agent, eingang);
                    Stoppable stopper = state.schedule.scheduleRepeating(agent);
                    agent.setStopper(stopper);
                }
            });

        }


        // Sanitäter hinzufügen (5 Personen)
        for (int i = 0; i < medicCount; i++) {
            Int2D pos = getRandomFreePosition();
            Person medic = new Person(Person.PersonType.MEDIC);
            medic.setEvent(this);
            agents.add(medic);
            grid.setObjectLocation(medic, pos.x, pos.y);
            Stoppable stopper = schedule.scheduleRepeating(medic);
            medic.setStopper(stopper);
        }

        // Security hinzufügen (5 Personen)
        for (int i = 0; i < securityCount; i++) {
            Int2D pos = getRandomFreePosition();
            Person security = new Person(Person.PersonType.SECURITY);
            security.setEvent(this);
            agents.add(security);
            grid.setObjectLocation(security, pos.x, pos.y);
            Stoppable stopper = schedule.scheduleRepeating(security);
            security.setStopper(stopper);
        }

        System.out.println(medicCount + " Sanitäter und " + securityCount + " Security-Personen wurden zur Simulation hinzugefügt.");

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
                .filter(z -> (z.getType() == Zone.ZoneType.EXIT || z.getType() == Zone.ZoneType.EMERGENCY_EXIT) && !z.isFull())
                .min((z1, z2) -> {
                    int d1 = Math.abs(z1.getPosition().x - fromPosition.x) + Math.abs(z1.getPosition().y - fromPosition.y);
                    int d2 = Math.abs(z2.getPosition().x - fromPosition.x) + Math.abs(z2.getPosition().y - fromPosition.y);
                    return Integer.compare(d1, d2);
                })
                .orElse(null);
    }

    private Int2D getRandomFreePosition() {
        Int2D pos;
        do {
            int x = random.nextInt(grid.getWidth());
            int y = random.nextInt(grid.getHeight());
            pos = new Int2D(x, y);
        } while (getZoneByPosition(pos) != null);
        return pos;
    }

    public EventSoundSystem getSoundSystem() {
        return soundSystem;
    }

    public void triggerFireAlarm(sim.util.Int2D fireLocation) {
        if (soundSystem != null) {
            soundSystem.playSound(SoundType.FIRE_ALARM, -1); // Endlos-Alarm
            System.out.println("FEUERALARM ausgelöst bei " + fireLocation);
            dispatchFireTruckToFire(fireLocation);
        }
    }

    public void triggerStormAlert() {
        if (soundSystem != null) {
            soundSystem.playSound(SoundType.STORM_WARNING, 30);
            System.out.println("Sturm-Warnung ausgelöst");
        }
    }

    @Override
    public void finish() {
        super.finish();
        if (soundSystem != null) {
            soundSystem.shutdown();
        }
    }

    public FireStation getFireStation() {
        return fireStation;
    }

    public void dispatchFireTruckToFire(Int2D fireLocation) {
        if (fireStation != null) {
            FireTruck truck = fireStation.dispatchFireTruck(fireLocation);
            System.out.println("Feuerwehrauto wurde zu " + fireLocation + " entsandt");
        }
    }
}
