package org.simulation;

/**
 * Repräsentiert eine eingeschränkte Zone (Restricted Area) im Eventgelände.
 * Diese kann aktiviert oder deaktiviert werden und besitzt einen Mittelpunkt sowie einen Radius.
 * Wird z. B. genutzt, um Sperrzonen bei Gefahren (Feuer, Kampf etc.) zu simulieren.
 *
 * @author Dorothea Ziegler
 */
public class RestrictedArea {
    private boolean active = true;
    private final int centerX;
    private final int centerY;
    private final int radius;

    public RestrictedArea(int centerX, int centerY, int radius) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.radius = radius;
    }

    public int getCenterX() {
        return centerX;
    }

    public int getCenterY() {
        return centerY;
    }

    public int getRadius() {
        return radius;
    }

    public boolean isActive() {
        return active;
    }

    public void deactivate() {
        this.active = false;
    }

    public boolean isInside(int x, int y) {
        int dx = x - centerX;
        int dy = y - centerY;
        return dx * dx + dy * dy <= radius * radius;
    }

    public void activate() {
        this.active = true;
    }

}