package org.simulation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RestrictedAreaTest {

    @Test
    public void testIsInsideWithinRadius() {
        RestrictedArea ra = new RestrictedArea(10, 10, 5);
        assertTrue(ra.isInside(12, 12), "Punkt innerhalb des Radius sollte true ergeben");
    }

    @Test
    public void testIsInsideOutsideRadius() {
        RestrictedArea ra = new RestrictedArea(10, 10, 3);
        assertFalse(ra.isInside(20, 20), "Punkt außerhalb des Radius sollte false ergeben");
    }

    @Test
    public void testDeactivate() {
        RestrictedArea ra = new RestrictedArea(5, 5, 2);
        assertTrue(ra.isActive(), "Initial sollte aktiv sein");
        ra.deactivate();
        assertFalse(ra.isActive(), "Nach Deaktivierung sollte nicht mehr aktiv sein");
    }

    @Test
    public void testCenterValues() {
        RestrictedArea ra = new RestrictedArea(3, 4, 6);
        assertEquals(3, ra.getCenterX());
        assertEquals(4, ra.getCenterY());
        assertEquals(6, ra.getRadius());
    }
}
