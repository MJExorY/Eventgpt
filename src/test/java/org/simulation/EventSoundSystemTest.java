package org.simulation;

import org.simulation.sound.EventSoundSystem;
import org.simulation.sound.SoundType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Timeout;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.TimeUnit;


public class EventSoundSystemTest {
    private EventSoundSystem soundSystem;

    @BeforeEach
    void setUp() {
        soundSystem = new EventSoundSystem();
        System.out.println("Test Setup: Sound-System erstellt");
    }

    @AfterEach
    void tearDown() {
        if (soundSystem != null) {
            soundSystem.shutdown();
            System.out.println("Test Teardown: Sound-System heruntergefahren");
        }
    }

    @Test
    @DisplayName("Fire Alarm sollte erfolgreich abgespielt werden")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testFireAlarmPlayback() {
        // Arrange
        SoundType soundType = SoundType.FIRE_ALARM;
        int duration = 2;

        // Act
        soundSystem.playSound(soundType, duration);

        // Assert
        assertTrue(soundSystem.isSoundPlaying(soundType),
                "Fire Alarm sollte nach dem Start spielen");

        // Kurz warten um sicherzustellen dass Sound läuft
        waitSafely(200);

        assertTrue(soundSystem.isSoundPlaying(soundType),
                "Fire Alarm sollte nach 200ms noch spielen");
    }

    @Test
    @DisplayName("Lautstärke-Kontrolle sollte korrekt funktionieren")
    void testVolumeControl() {
        // Arrange
        SoundType soundType = SoundType.FIRE_ALARM;
        float testVolume = 0.5f;
        int duration = 1;

        // Act
        soundSystem.setVolume(soundType, testVolume);
        soundSystem.playSound(soundType, duration);

        // Assert
        assertTrue(soundSystem.isSoundPlaying(soundType),
                "Sound sollte nach Lautstärke-Einstellung spielen");
    }

    @Test
    @DisplayName("Alle Sounds stoppen sollte funktionieren")
    void testStopAllSounds() {
        // Arrange - Mehrere Sounds starten
        soundSystem.playSound(SoundType.FIRE_ALARM, -1);
        soundSystem.playSound(SoundType.FIRE_TRUCK_SIREN, -1);
        soundSystem.playSound(SoundType.STORM_WARNING, -1);

        // Kurz warten bis Sounds laufen
        waitSafely(100);

        // Verify sounds are playing
        assertTrue(soundSystem.isSoundPlaying(SoundType.FIRE_ALARM),
                "Fire Alarm sollte spielen");
        assertTrue(soundSystem.isSoundPlaying(SoundType.FIRE_TRUCK_SIREN),
                "Fire Truck Siren sollte spielen");
        assertTrue(soundSystem.isSoundPlaying(SoundType.STORM_WARNING),
                "Storm Warning sollte spielen");

        // Act - Alle Sounds stoppen
        soundSystem.stopAllSounds();

        // Assert - Alle Sounds sollten gestoppt sein
        assertFalse(soundSystem.isSoundPlaying(SoundType.FIRE_ALARM),
                "Fire Alarm sollte gestoppt sein");
        assertFalse(soundSystem.isSoundPlaying(SoundType.FIRE_TRUCK_SIREN),
                "Fire Truck Siren sollte gestoppt sein");
        assertFalse(soundSystem.isSoundPlaying(SoundType.STORM_WARNING),
                "Storm Warning sollte gestoppt sein");
    }

    @Test
    @DisplayName("Sound An/Aus Toggle sollte funktionieren")
    void testSoundToggle() {
        // Arrange
        SoundType soundType = SoundType.FIRE_ALARM;
        int duration = 5;

        // Act - Sound ausschalten und versuchen zu spielen
        soundSystem.setSoundEnabled(false);
        soundSystem.playSound(soundType, duration);

        // Assert - Sound sollte nicht spielen da deaktiviert
        assertFalse(soundSystem.isSoundPlaying(soundType),
                "Sound sollte nicht spielen wenn System deaktiviert");

        // Act - Sound wieder einschalten und spielen
        soundSystem.setSoundEnabled(true);
        soundSystem.playSound(soundType, duration);

        // Assert - Sound sollte jetzt spielen
        assertTrue(soundSystem.isSoundPlaying(soundType),
                "Sound sollte spielen wenn System aktiviert");
    }

    @Test
    @DisplayName("Einzelner Sound stoppen sollte funktionieren")
    void testStopSpecificSound() {
        // Arrange - Zwei verschiedene Sounds starten
        soundSystem.playSound(SoundType.FIRE_ALARM, -1);
        soundSystem.playSound(SoundType.STORM_WARNING, -1);

        waitSafely(100);

        // Verify beide spielen
        assertTrue(soundSystem.isSoundPlaying(SoundType.FIRE_ALARM));
        assertTrue(soundSystem.isSoundPlaying(SoundType.STORM_WARNING));

        // Act - Nur einen Sound stoppen
        soundSystem.stopSound(SoundType.FIRE_ALARM);

        // Assert - Nur Fire Alarm sollte gestoppt sein
        assertFalse(soundSystem.isSoundPlaying(SoundType.FIRE_ALARM),
                "Fire Alarm sollte gestoppt sein");
        assertTrue(soundSystem.isSoundPlaying(SoundType.STORM_WARNING),
                "Storm Warning sollte noch spielen");
    }

    @Test
    @DisplayName("Zeitlich begrenzter Sound sollte automatisch stoppen")
    @Timeout(value = 8, unit = TimeUnit.SECONDS)
    void testTimedSoundAutoStop() {
        // Arrange
        SoundType soundType = SoundType.STORM_WARNING;
        int duration = 1; // 1 Sekunde

        // Act
        soundSystem.playSound(soundType, duration);

        // Assert - Sollte initial spielen
        assertTrue(soundSystem.isSoundPlaying(soundType),
                "Sound sollte initial spielen");

        // Wait for sound to finish (1s + buffer)
        waitSafely(1500);

        // Assert - Sollte automatisch gestoppt sein
        assertFalse(soundSystem.isSoundPlaying(soundType),
                "Sound sollte nach Ablauf automatisch gestoppt sein");
    }

    @Test
    @DisplayName("Lautstärke-Grenzen sollten eingehalten werden")
    void testVolumeConstraints() {
        // Arrange
        SoundType soundType = SoundType.FIRE_ALARM;

        // Act & Assert - Teste verschiedene Lautstärke-Werte
        soundSystem.setVolume(soundType, -0.5f); // Unter Minimum
        soundSystem.setVolume(soundType, 1.5f);  // Über Maximum
        soundSystem.setVolume(soundType, 0.5f);  // Gültiger Wert

        // Keine Exception sollte geworfen werden
        assertDoesNotThrow(() -> soundSystem.playSound(soundType, 1), "Message");
    }

    @Test
    @DisplayName("Mehrfaches Starten desselben Sounds sollte vorherigen stoppen")
    void testMultiplePlaySameSound() {
        // Arrange
        SoundType soundType = SoundType.FIRE_ALARM;

        // Act - Starte denselben Sound mehrfach
        soundSystem.playSound(soundType, -1);
        assertTrue(soundSystem.isSoundPlaying(soundType), "Erster Sound sollte spielen");

        soundSystem.playSound(soundType, -1);
        assertTrue(soundSystem.isSoundPlaying(soundType), "Zweiter Sound sollte spielen");

        // Assert - Nur ein Sound sollte laufen (kein Overlap)
        int playingCount = 0;
        for (SoundType type : SoundType.values()) {
            if (soundSystem.isSoundPlaying(type)) {
                playingCount++;
            }
        }
        assertEquals(1, playingCount, "Nur ein Sound sollte gleichzeitig spielen");
    }


    private void waitSafely(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Test wurde unterbrochen: " + e.getMessage());
        }
    }
}