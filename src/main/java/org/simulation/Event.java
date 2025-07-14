package org.simulation;

import events.Disturbance;

import metrics.DefaultMetricsCollector;
import metrics.MetricsCollector;
import metrics.MetricsViewer;
import sounds.EventSoundSystem;
import sounds.SoundType;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.field.grid.SparseGrid2D;
import sim.util.Bag;
import sim.util.Int2D;
import zones.*;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;


/**
 * Die zentrale Simulationsumgebung für ein Großevent mit Besuchern, Security, Sanitätern und möglichen Störungen.
 * Verwaltet das Grid, alle Agenten, Zonen, Störungen und die Zeitsteuerung der Simulation.
 * Unterstützt das Auslösen von Alarmen, Spawnen von Störungen, das Sammeln von Metriken sowie Notfallreaktionen.
 * Kernkomponenten:
 * - Besucher-, Security- und Sanitäter-Agenten
 * - Unterschiedliche Zonen (z. B. FOOD, WC, Ausgänge)
 * - Grid (SparseGrid2D) zur räumlichen Positionierung
 * - EventSoundSystem für akustisches Feedback
 * - MetricsCollector zur Analyse nach Simulationsende
 *
 * @author Lukas Kilian
 */
public class Event extends SimState {

    private final int visitorCount;
    private final int medicCount;
    private final int securityCount;
    public SparseGrid2D grid;
    public final List<Zone> zones = new ArrayList<>();
    public final List<Agent> agents = new ArrayList<>();
    private final List<Disturbance> disturbances = new ArrayList<>();
    private MetricsCollector metricsCollector;

    private final EventSoundSystem soundSystem;
    private FireStation fireStation;
    private final transient List<RestrictedArea> restrictedAreas = new ArrayList<>();

    private boolean stormAlertTriggered = false; // NEU

    public Event(long seed, int visitorCount, int medicCount, int securityCount,
                 MetricsCollector collector) {
        super(seed);
        this.metricsCollector = collector;
        this.visitorCount = visitorCount;
        this.medicCount = medicCount;
        this.securityCount = securityCount;
        this.soundSystem = new EventSoundSystem();
    }

    public static void main(String[] args) {
        MetricsCollector collector = new DefaultMetricsCollector();

        for (Zone.ZoneType type : Zone.ZoneType.values()) {
            collector.registerMetric("ZoneEntry_" + type);
            collector.registerMetric("ZoneExit_" + type);
            collector.registerMetric("PanicEscape_" + type);
            collector.registerMetric("TimeInZone_" + type);
            collector.registerMetric("QueueWait_" + type);
            collector.registerMetric("PanicDuration");
        }

        for (String evt : List.of("FIRE", "FIGHT", "STORM")) {
            collector.registerMetric("EventTriggered_" + evt);
        }

        Event sim = new Event(System.currentTimeMillis(), 15, 5, 5, collector);
        sim.start();

        for (int i = 0; i < 10; i++) {
            sim.schedule.step(sim);
        }

        sim.finish();
        System.out.println("Event-Simulation fertig.");

        System.out.println("\n--- Metriken ---");
        sim.getCollector().getAllMetrics()
                .forEach((key, values) -> System.out.println(key + ": " + values.size()));
    }

    @Override
    public void start() {
        super.start();

        grid = new SparseGrid2D(100, 100);

        Zone foodZone = new Zone(Zone.ZoneType.FOOD, new Int2D(5, 15), 3);
        Zone wcZone = new Zone(Zone.ZoneType.WC, new Int2D(90, 25), 3);
        Zone actMain = new Zone(Zone.ZoneType.ACT_MAIN, new Int2D(50, 45), 20);
        Zone actSide = new Zone(Zone.ZoneType.ACT_SIDE, new Int2D(15, 85), 15);
        Zone normalExit = new Zone(Zone.ZoneType.EXIT, new Int2D(60, 90), Integer.MAX_VALUE);
        Zone emergencyNorth = new Zone(Zone.ZoneType.EMERGENCY_EXIT, new Int2D(50, 5), 5);
        Zone emergencyEast = new Zone(Zone.ZoneType.EMERGENCY_EXIT, new Int2D(95, 50), 5);
        Zone emergencyWest = new Zone(Zone.ZoneType.EMERGENCY_EXIT, new Int2D(5, 50), 5);

        zones.addAll(List.of(
                foodZone, wcZone, actMain, actSide, normalExit,
                emergencyNorth, emergencyEast, emergencyWest
        ));

        EmergencyRouteStraight emergencyRouteStraight = new EmergencyRouteStraight(new Int2D(50, 10));
        grid.setObjectLocation(emergencyRouteStraight, emergencyRouteStraight.getPosition());
        schedule.scheduleRepeating(emergencyRouteStraight);


        //   Zone emergencySouth = new Zone(Zone.ZoneType.EMERGENCY_EXIT, new Int2D(30, 95), Integer.MAX_VALUE); // Süden (links vom normalen Exit)
        emergencyEast = new Zone(Zone.ZoneType.EMERGENCY_EXIT, new Int2D(95, 50), Integer.MAX_VALUE);    // Osten


        EmergencyRouteRechts emergencyRouteRight = new EmergencyRouteRechts(new Int2D(83, 50));
        grid.setObjectLocation(emergencyRouteRight, emergencyRouteRight.getPosition());
        schedule.scheduleRepeating(emergencyRouteRight);

        emergencyWest = new Zone(Zone.ZoneType.EMERGENCY_EXIT, new Int2D(5, 50), Integer.MAX_VALUE);     // Westen

        EmergencyRouteLinks emergencyRouteLinks = new EmergencyRouteLinks(new Int2D(9, 50));
        grid.setObjectLocation(emergencyRouteLinks, emergencyRouteLinks.getPosition());
        schedule.scheduleRepeating(emergencyRouteLinks);

        //  Zone emergencyNorthEast = new Zone(Zone.ZoneType.EMERGENCY_EXIT, new Int2D(85, 15), Integer.MAX_VALUE); // Nordosten
        //  Zone emergencySouthWest = new Zone(Zone.ZoneType.EMERGENCY_EXIT, new Int2D(95, 95), Integer.MAX_VALUE); // Südost

        zones.addAll(List.of(foodZone, wcZone, actMain, actSide, normalExit,
                emergencyNorth, emergencyEast, emergencyWest));


        // Alle Zonen im Grid sichtbar machen
        for (Zone z : zones) {
            grid.setObjectLocation(z, z.getPosition().x, z.getPosition().y);
        }

        EmergencyRouteStraight routeStraight = new EmergencyRouteStraight(new Int2D(50, 10));
        EmergencyRouteRechts routeRight = new EmergencyRouteRechts(new Int2D(83, 50));
        EmergencyRouteLinks routeLinks = new EmergencyRouteLinks(new Int2D(9, 50));

        grid.setObjectLocation(routeStraight, routeStraight.getPosition());
        grid.setObjectLocation(routeRight, routeRight.getPosition());
        grid.setObjectLocation(routeLinks, routeLinks.getPosition());

        schedule.scheduleRepeating(routeStraight);
        schedule.scheduleRepeating(routeRight);
        schedule.scheduleRepeating(routeLinks);

        fireStation = new FireStation(new Int2D(95, 70), this);
        grid.setObjectLocation(fireStation, fireStation.getPosition().x,
                fireStation.getPosition().y);
        System.out.println("Feuerwache wurde bei " + fireStation.getPosition() + " erstellt");
        Int2D eingang = new Int2D(60, 90); // Eingang Zone

        for (int i = 0; i < visitorCount; i++) {
            schedule.scheduleOnce(i, new Steppable() {
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
        // RestrictedAreas ins Grid setzen → damit sie gezeichnet werden können
        for (RestrictedArea ra : restrictedAreas) {
            grid.setObjectLocation(ra, new Int2D(ra.getCenterX(), ra.getCenterY()));
        }

        System.out.println(
                medicCount + " Sanitäter und " + securityCount + " Security-Personen wurden zur Simulation hinzugefügt.");
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

    public MetricsCollector getCollector() {
        return metricsCollector;
    }

    public void setCollector(MetricsCollector collector) {
        this.metricsCollector = collector;
    }

    public void spawn(Disturbance disturbance) {
        if (disturbance.getPosition() != null) {
            grid.setObjectLocation(disturbance, disturbance.getPosition().x,
                    disturbance.getPosition().y);
        }
        disturbances.add(disturbance);
        Stoppable stopper = schedule.scheduleRepeating(disturbance);
        disturbance.setStopper(stopper);
    }

    public Zone getNearestAvailableExit(Int2D fromPosition) {
        return zones.stream()
                .filter(z -> (z.getType() == Zone.ZoneType.EXIT || z.getType() == Zone.ZoneType.EMERGENCY_EXIT) && !z.isFull())
                .min((z1, z2) -> {
                    int d1 = Math.abs(z1.getPosition().x - fromPosition.x) + Math.abs(
                            z1.getPosition().y - fromPosition.y);
                    int d2 = Math.abs(z2.getPosition().x - fromPosition.x) + Math.abs(
                            z2.getPosition().y - fromPosition.y);
                    return Integer.compare(d1, d2);
                })
                .orElse(null);
    }

    public Zone getNearestAvailableEmergencyExit(Int2D fromPosition) {
        return zones.stream()
                .filter(z -> z.getType() == Zone.ZoneType.EMERGENCY_EXIT)
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

    public void triggerFireAlarm(Int2D fireLocation) {
        if (soundSystem != null) {
            soundSystem.playSound(SoundType.FIRE_ALARM, -1);
            System.out.println("FEUERALARM ausgelöst bei " + fireLocation);
            dispatchFireTruckToFire(fireLocation);
        }
    }

    public void triggerStormAlert() {
        if (soundSystem != null) {
            soundSystem.playSound(SoundType.STORM_WARNING, 30);
            System.out.println("Sturm-Warnung ausgelöst");
        }
        this.stormAlertTriggered = true; // NEU
    }

    public boolean isStormAlertTriggered() { // NEU
        return stormAlertTriggered;
    }

    @Override
    public void finish() {
        super.finish();

        SwingUtilities.invokeLater(() -> MetricsViewer.show(metricsCollector));

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

    public void addRestrictedArea(RestrictedArea ra) {
        restrictedAreas.add(ra);
    }

    public List<RestrictedArea> getRestrictedAreas() {
        return restrictedAreas;
    }


}
