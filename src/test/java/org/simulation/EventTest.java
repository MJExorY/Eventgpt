package org.simulation;

import metrics.DefaultMetricsCollector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sim.field.grid.SparseGrid2D;
import sim.util.Bag;
import sim.util.Int2D;

import static org.junit.jupiter.api.Assertions.*;

public class EventTest {

    private Event event;

    @BeforeEach
    public void setUp() {
        DefaultMetricsCollector collector = new DefaultMetricsCollector();
        for (Zone.ZoneType type : Zone.ZoneType.values()) {
            collector.registerMetric("ZoneEntry_" + type);
            collector.registerMetric("ZoneExit_" + type);
            collector.registerMetric("PanicEscape_" + type);
        }

        event = new Event(System.currentTimeMillis(), 0, 0, 0, collector);
        event.start(); // Initialisiert das Event, Zonen etc.
    }

    @Test
    public void testZonenInitialisierung() {
        // Überprüfe, ob alle Zonen vorhanden sind
        assertNotNull(event.getZoneByType(Zone.ZoneType.FOOD));
        assertNotNull(event.getZoneByType(Zone.ZoneType.ACT_MAIN));
        assertNotNull(event.getZoneByType(Zone.ZoneType.ACT_SIDE));
        assertNotNull(event.getZoneByType(Zone.ZoneType.EXIT));
    }

    @Test
    public void testZonenPositionen() {
        // Überprüfe die Positionen der Zonen
        Zone food = event.getZoneByType(Zone.ZoneType.FOOD);
        assertEquals(new Int2D(5, 15), food.getPosition());

        Zone actMain = event.getZoneByType(Zone.ZoneType.ACT_MAIN);
        assertEquals(new Int2D(50, 45), actMain.getPosition());

        Zone actSide = event.getZoneByType(Zone.ZoneType.ACT_SIDE);
        assertEquals(new Int2D(15, 85), actSide.getPosition());

        Zone exit = event.getZoneByType(Zone.ZoneType.EXIT);
        assertEquals(new Int2D(60, 90), exit.getPosition());
    }

    @Test
    public void testZonenImGrid() {
        SparseGrid2D grid = event.grid;

        // Liste der Zone-Typen, die überprüft werden sollen
        Zone.ZoneType[] zoneTypes = {
                Zone.ZoneType.FOOD,
                Zone.ZoneType.ACT_MAIN,
                Zone.ZoneType.ACT_SIDE,
                Zone.ZoneType.EXIT
        };

        for (Zone.ZoneType type : zoneTypes) {
            Zone zone = event.getZoneByType(type);
            assertNotNull(zone, "Zone vom Typ " + type + " sollte existieren");

            // Hole die Position der Zone
            Int2D zonePos = zone.getPosition();

            // Hole alle Objekte an der Position der Zone
            Bag objectsAtPos = grid.getObjectsAtLocation(zonePos.x, zonePos.y);

            // Überprüfe, ob das Zone-Objekt an dieser Position im Grid vorhanden ist
            boolean foundZoneObject = false;
            for (int i = 0; i < objectsAtPos.size(); i++) {
                Object obj = objectsAtPos.get(i);
                if (obj == zone) {
                    foundZoneObject = true;
                    break;
                }
            }
            assertTrue(foundZoneObject, "Das Grid enthält das Objekt der Zone " + type + " an der erwarteten Position");

            // Optional: Überprüfe explizit mit getObjectLocation(Object)
            Object objInGrid = grid.getObjectLocation(zone);
            assertNotNull(objInGrid, "Das Objekt der Zone " + type + " sollte im Grid vorhanden sein");
            assertEquals(zone.getPosition(), objInGrid, "Das Objekt im Grid stimmt nicht mit der Zone überein");
        }
    }

    @Test
    public void testAgentenErzeugung() {
        event.agents.clear();
        for (int i = 0; i < 1; i++) {
            event.schedule.step(event);
        }

        assertEquals(0, event.agents.size(), "Es wurde kein Agent erzeugt");

        for (Agent agent : event.agents) {
            Object locObj = event.grid.getObjectLocation(agent);
            assertNotNull(locObj, "Agent ist nicht im Grid platziert: " + agent);
        }

    }

}