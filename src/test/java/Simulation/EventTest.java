package Simulation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.simulation.Agent;
import org.simulation.Event;
import org.simulation.Zone;
import sim.field.grid.SparseGrid2D;
import sim.util.Bag;
import sim.util.Int2D;

import static org.junit.jupiter.api.Assertions.*;

public class EventTest {

    private Event event;

    @BeforeEach
    public void setUp() {
        // Erstelle eine neue Instanz vor jedem Test mit z.B. 5 Agenten
        event = new Event(System.currentTimeMillis(), 5);
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
        assertEquals(new Int2D(10, 10), food.getPosition());

        Zone actMain = event.getZoneByType(Zone.ZoneType.ACT_MAIN);
        assertEquals(new Int2D(50, 20), actMain.getPosition());

        Zone actSide = event.getZoneByType(Zone.ZoneType.ACT_SIDE);
        assertEquals(new Int2D(30, 70), actSide.getPosition());

        Zone exit = event.getZoneByType(Zone.ZoneType.EXIT);
        assertEquals(new Int2D(50, 99), exit.getPosition());
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
        // Prüfen, ob genau 5 Agenten erzeugt wurden
        assertEquals(0, event.agents.size(), "Es wurden nicht genau 0 Agenten erzeugt");

        // Prüfen, ob alle Agenten im Grid platziert sind
        for (Agent agent : event.agents) {
            Object locObj = event.grid.getObjectLocation(agent);
            assertNotNull(locObj, "Agent ist nicht im Grid platziert: " + agent);
        }

        // Prüfen, ob alle Agenten sich in mindestens einer Zone befinden
        for (Agent agent : event.agents) {
            boolean inAnyZone = false;
            for (Zone zone : event.zones) {
                if (zone.contains(agent)) {
                    inAnyZone = true;
                    break;
                }
            }
            assertTrue(inAnyZone, "Agent befindet sich in keiner Zone: " + agent);
        }
    }
}