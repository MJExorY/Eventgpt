package org.simulation.sound;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class EventSoundSystem {
    private final Map<SoundType, Boolean> playingStatus = new ConcurrentHashMap<>();
    private final Map<SoundType, ScheduledFuture<?>> soundTasks = new ConcurrentHashMap<>();
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(4);

    private final AudioPlayer audioPlayer;
    private boolean useRealAudio = true;

    public EventSoundSystem() {
        audioPlayer = new AudioPlayer();
        initializeSoundSystem();
    }

    private void initializeSoundSystem() {
        try {
            for (SoundType soundType : SoundType.values()) {
                playingStatus.put(soundType, false);
            }

            if (!AudioPlayer.isAudioAvailable()) {
                System.out.println("Kein Audio-System verfügbar - verwende Simulation");
                useRealAudio = false;
            } else {
                System.out.println("Audio-System verfügbar - verwende echte Sounds");
            }

            System.out.println("Sound-System initialisiert (Modus: " +
                    (useRealAudio ? "Echt" : "Simulation") + ")");
        } catch (Exception e) {
            System.err.println("Fehler bei Sound-System Initialisierung: " + e.getMessage());
            useRealAudio = false;
        }
    }

    public void playSound(SoundType soundType, int duration) {
        if (soundType == null) {
            return;
        }

        stopSound(soundType);
        playingStatus.put(soundType, true);

        String durationText = (duration == -1) ? "Endlos" : duration + "s";
        System.out.println("SOUND: " + soundType.name() + " wird abgespielt (Dauer: " + durationText + ")");

        if (useRealAudio) {
            boolean isLooping = soundType.isLooping() && duration == -1;
            audioPlayer.playSound(soundType, isLooping);
        }

        if (soundType.isLooping() && duration == -1) {
            scheduleLoopingSound(soundType);
        } else {
            scheduleTimedSound(soundType, duration);
        }
    }

    private void scheduleLoopingSound(SoundType soundType) {
        ScheduledFuture<?> task = executor.scheduleWithFixedDelay(() -> {
            if (playingStatus.getOrDefault(soundType, false)) {
                System.out.println("SOUND: " + soundType.name() + " läuft weiter...");
            }
        }, 0, 5, TimeUnit.SECONDS);

        soundTasks.put(soundType, task);
    }

    private void scheduleTimedSound(SoundType soundType, int duration) {
        if (duration <= 0) {
            return;
        }

        ScheduledFuture<?> task = executor.schedule(() -> {
            playingStatus.put(soundType, false);
            System.out.println("SOUND: " + soundType.name() + " beendet");
            soundTasks.remove(soundType);
        }, duration, TimeUnit.SECONDS);

        soundTasks.put(soundType, task);
    }

    private void stopSound(SoundType soundType) {
        playingStatus.put(soundType, false);

        ScheduledFuture<?> task = soundTasks.remove(soundType);
        if (task != null && !task.isDone()) {
            task.cancel(false);
        }
    }

    public void shutdown() {
        for (SoundType soundType : SoundType.values()) {
            stopSound(soundType);
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }

        if (audioPlayer != null) {
            audioPlayer.shutdown();
        }

        System.out.println("Sound-System heruntergefahren");
    }
}