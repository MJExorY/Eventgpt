package org.simulation;

import org.simulation.Event;
import org.simulation.sound.EventSoundSystem;
import org.simulation.sound.SoundType;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.util.Int2D;

import java.awt.Color;

public class FireTruck implements Steppable {
    private Int2D currentPosition;
    private final Int2D targetPosition;
    private boolean isMoving = false;
    private boolean arrivedAtFire = false;
    private final Event event;
    private Stoppable stopper;
    private final EventSoundSystem soundSystem;
    private int moveSpeed = 1; // Geschwindigkeit: 1 Feld pro Step
    private int stepCounter = 0;

    public FireTruck(Int2D startPosition, Int2D firePosition, Event event) {
        this.currentPosition = startPosition;
        this.targetPosition = firePosition;
        this.event = event;
        this.soundSystem = event.getSoundSystem();
        this.isMoving = true;

        // Feuerwehr-Sirene starten
        if (soundSystem != null) {
            soundSystem.playSound(SoundType.FIRE_TRUCK_SIREN, -1); // Endlos bis Ankunft
        }

        System.out.println("Feuerwehrauto startet von " + startPosition + " Richtung " + firePosition);
    }

    @Override
    public void step(SimState state) {
        if (!isMoving || arrivedAtFire) {
            return;
        }

        // Bewegung zum Ziel
        moveTowardsTarget();

        // Prüfen ob angekommen
        if (currentPosition.equals(targetPosition)) {
            arriveAtFire();
        }
    }

    private void moveTowardsTarget() {
        stepCounter++;

        // Nur jeden 2. Step bewegen (halbe Geschwindigkeit)
        if (stepCounter % 6 != 0) {
            return;
        }

        int dx = Integer.compare(targetPosition.x, currentPosition.x);
        int dy = Integer.compare(targetPosition.y, currentPosition.y);

        int newX = currentPosition.x + (dx * moveSpeed);
        int newY = currentPosition.y + (dy * moveSpeed);

        newX = Math.max(0, Math.min(event.grid.getWidth() - 1, newX));
        newY = Math.max(0, Math.min(event.grid.getHeight() - 1, newY));

        Int2D newPosition = new Int2D(newX, newY);
        event.grid.setObjectLocation(this, newPosition);
        currentPosition = newPosition;

        System.out.println("Feuerwehrauto bewegt sich: " + currentPosition + " → Ziel: " + targetPosition);
    }

    private void arriveAtFire() {
        arrivedAtFire = true;
        isMoving = false;

        // Sirene stoppen
        if (soundSystem != null) {
            System.out.println("Feuerwehrauto ist am Feuer angekommen - Sirene läuft weiter für Löscharbeiten");
        }

        System.out.println("Feuerwehrauto ist am Feuer angekommen bei " + currentPosition);
        System.out.println("Feuerwehrauto ist bereit für Löscharbeiten!");

        // Löscharbeiten
    }

    // Getter & Setter
    public Int2D getCurrentPosition() {
        return currentPosition;
    }

    public Int2D getTargetPosition() {
        return targetPosition;
    }

    public boolean isMoving() {
        return isMoving;
    }

    public boolean hasArrivedAtFire() {
        return arrivedAtFire;
    }

    public void setStopper(Stoppable stopper) {
        this.stopper = stopper;
    }

    public void setMoveSpeed(int speed) {
        this.moveSpeed = Math.max(1, speed);
    }

    public Color getColor() {
        return Color.RED;
    }

}
