package org.simulation;

import events.FireDisturbance;
import states.IStates;
import states.RoamingState;
import sounds.EventSoundSystem;
import sounds.SoundType;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.util.Bag;
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

        if (soundSystem != null) {
            soundSystem.playSound(SoundType.FIRE_TRUCK_SIREN, -1);
        }

        System.out.println("Feuerwehrauto startet von " + startPosition + " Richtung " + firePosition);
    }

    @Override
    public void step(SimState state) {
        if (!isMoving || arrivedAtFire) {
            return;
        }

        moveTowardsTarget();

        if (currentPosition.equals(targetPosition)) {
            arriveAtFire();
        }
    }

    private void moveTowardsTarget() {
        stepCounter++;

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

        System.out.println("Feuerwehrauto ist am Feuer angekommen bei " + currentPosition);
        System.out.println("Feuerwehrauto beginnt mit Löscharbeiten!");

        int duration = 10 + event.random.nextInt(6); // 10–15
        event.schedule.scheduleOnce(event.schedule.getTime() + duration, new Steppable() {
            @Override
            public void step(SimState state) {
                extinguishFire();
            }
        });
    }


    private void resetPanicForAgentsNear(Int2D position, int radius) {
        int startX = Math.max(0, position.x - radius);
        int endX = Math.min(event.grid.getWidth() - 1, position.x + radius);
        int startY = Math.max(0, position.y - radius);
        int endY = Math.min(event.grid.getHeight() - 1, position.y + radius);

        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                double distance = position.distance(new Int2D(x, y));
                if (distance <= radius) {
                    Bag objects = event.grid.getObjectsAtLocation(x, y);
                    if (objects != null) {
                        for (Object obj : objects) {
                            if (obj instanceof Agent agent) {
                                if (agent.isPanicking()) {
                                    agent.setPanicking(false);
                                    agent.setCurrentState(new RoamingState());
                                    System.out.println("Panik-Agent bei " + new Int2D(x, y) + " beruhigt.");
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void extinguishFire() {
        System.out.println("Feuerwehrauto hat das Feuer gelöscht bei " + currentPosition);

        if (soundSystem != null) {
            soundSystem.stopFireTruckSiren();
            soundSystem.stopFireAlarm();
            System.out.println("Sirene wurde gestoppt.");
        }
        FireDisturbance fire = null;

        Object obj = event.grid.getObjectsAtLocation(currentPosition).stream()
                .filter(o -> o instanceof FireDisturbance)
                .findFirst()
                .orElse(null);

        if (obj instanceof FireDisturbance) {
            fire = (FireDisturbance) obj;
            fire.resolve(event);
            event.grid.remove(fire);
            System.out.println("Feuerobjekt wurde entfernt.");
        }
        if (fire != null) {
            resetPanicForAgentsNear(fire.getPosition(), fire.getPanicRadius());
        }

        event.grid.remove(this);
        if (stopper != null) {
            stopper.stop();
        }
        System.out.println("Feuerwehrauto fährt zurück zur Wache (verschwindet).");

    }


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
