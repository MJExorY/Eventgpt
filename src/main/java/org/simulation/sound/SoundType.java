package org.simulation.sound;

public enum SoundType {

    FIRE_ALARM("fire_alarm.wav", true),
    FIRE_TRUCK_SIREN("fire_truck_siren.wav", true),
    STORM_WARNING("storm_warning.wav", true);

    private final String fileName;
    private final boolean isLooping;

    SoundType(String fileName, boolean isLooping) {
        this.fileName = fileName;
        this.isLooping = isLooping;
    }

    public String getFileName() {
        return fileName;
    }

    public boolean isLooping() {
        return isLooping;
    }
}

