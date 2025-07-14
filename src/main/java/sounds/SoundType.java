package sounds;

/**
 * Die {@code SoundType}-Enum definiert alle verfügbaren Soundeffekte,
 * die in der Simulation verwendet werden können, zusammen mit den zugehörigen Dateinamen
 * und der Information, ob sie im Loop abgespielt werden sollen.
 * <p>
 * Diese Enum wird vom {@link AudioPlayer} und {@link EventSoundSystem} verwendet,
 * um Sounds zu identifizieren und zu verwalten.
 *
 * @author Betuel
 */
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

