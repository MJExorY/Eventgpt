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
    }

    @AfterEach
    void tearDown() {
        soundSystem.shutdown();
    }

    @Test
    void testSoundSystemInitialization() {
        assertNotNull(soundSystem);
    }

    @Test
    void testPlaySound() {
        // Test dass playSound keine Exception wirft
        assertDoesNotThrow(() -> {
            soundSystem.playSound(SoundType.FIRE_ALARM, 1);
        });
    }

    @Test
    void testShutdown() {
        // Test dass shutdown keine Exception wirft
        assertDoesNotThrow(() -> {
            soundSystem.shutdown();
        });
    }
}