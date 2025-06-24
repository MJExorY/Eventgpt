package org.simulation.sound;

import javax.sound.sampled.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class AudioPlayer {

    private final Map<SoundType, Clip> activeClips = new ConcurrentHashMap<>();
    private final Map<SoundType, Float> volumeSettings = new ConcurrentHashMap<>();

    public AudioPlayer() {
        // Standard-Lautstärke für alle Sound-Typen
        for (SoundType soundType : SoundType.values()) {
            volumeSettings.put(soundType, 0.7f);
        }
        System.out.println("AudioPlayer initialisiert");
    }

    public boolean playSound(SoundType soundType, boolean loop) {
        try {
            // Stoppe vorherigen Sound falls vorhanden
            stopSound(soundType);

            // Lade Audio-Datei
            String fileName = "/sounds/" + soundType.getFileName();
            InputStream audioStream = getClass().getResourceAsStream(fileName);

            if (audioStream == null) {
                System.err.println("Audio-Datei nicht gefunden: " + fileName);
                return false;
            }

            // Erstelle Audio-Input-Stream
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioStream);

            // Erstelle Clip
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);


            // Loop-Modus setzen
            if (loop) {
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            }

            // Clip starten
            clip.start();

            // Clip speichern für spätere Kontrolle
            activeClips.put(soundType, clip);

            System.out.println("AUDIO: " + soundType.name() + " wird abgespielt " +
                    "(Loop: " + loop + ", Volume: " + (int) (volumeSettings.get(soundType) * 100) + "%)");

            return true;

        } catch (UnsupportedAudioFileException e) {
            System.err.println(" Nicht unterstütztes Audio-Format für " + soundType + ": " + e.getMessage());
        } catch (IOException e) {
            System.err.println(" IO-Fehler beim Laden von " + soundType + ": " + e.getMessage());
        } catch (LineUnavailableException e) {
            System.err.println(" Audio-Line nicht verfügbar für " + soundType + ": " + e.getMessage());
        } catch (Exception e) {
            System.err.println(" Unerwarteter Fehler beim Abspielen von " + soundType + ": " + e.getMessage());
        }

        return false;
    }

    public void stopSound(SoundType soundType) {
        Clip clip = activeClips.remove(soundType);
        if (clip != null && clip.isRunning()) {
            clip.stop();
            clip.close();
            System.out.println("AUDIO: " + soundType.name() + " gestoppt");
        }
    }

    /**
     * Stoppt alle aktiven Sounds
     */
    public void stopAllSounds() {
        System.out.println("AUDIO: Alle Sounds werden gestoppt");
        for (SoundType soundType : activeClips.keySet()) {
            stopSound(soundType);
        }
        activeClips.clear();
    }

    /**
     * Prüft ob ein Sound gerade abgespielt wird
     */
    public boolean isSoundPlaying(SoundType soundType) {
        Clip clip = activeClips.get(soundType);
        return clip != null && clip.isRunning();
    }

    /**
     * Setzt die Lautstärke für einen Sound-Typ
     */
    public void setVolume(SoundType soundType, float volume) {
        // Begrenze Lautstärke auf 0.0 - 1.0
        volume = Math.max(0.0f, Math.min(1.0f, volume));
        volumeSettings.put(soundType, volume);

    }

    /**
     * Gibt alle Ressourcen frei
     */
    public void shutdown() {
        stopAllSounds();
        System.out.println("AudioPlayer heruntergefahren");
    }

    /**
     * Prüft ob Audio-System verfügbar ist
     */
    public static boolean isAudioAvailable() {
        try {
            return AudioSystem.getMixer(null) != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Gibt Information über verfügbare Audio-Mixer aus
     */
    public static void printAudioInfo() {
        try {
            Mixer.Info[] mixers = AudioSystem.getMixerInfo();
            System.out.println("Verfügbare Audio-Mixer: " + mixers.length);
            for (Mixer.Info mixer : mixers) {
                System.out.println("  - " + mixer.getName());
            }
        } catch (Exception e) {
            System.err.println("Fehler beim Abrufen der Audio-Info: " + e.getMessage());
        }
    }
}

