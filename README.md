# Event Simulation – Agentenbasierte Simulation mit MASON

Eine Java-basierte Ereignissimulation mit agentenbasierten Verhalten für Veranstaltungsbesucher, Security-Personal und Rettungskräfte.
Das Projekt nutzt das MASON-Framework (https://cs.gmu.edu/~eclab/projects/mason/) für Multi-Agent-Simulationen und bietet eine grafische Benutzeroberfläche zur Visualisierung und Interaktion.

## Inhaltsverzeichnis

- [Ziel & Zweck](#ziel--zweck)
- [Features](#features)
- [Projektstruktur](#projektstruktur)
- [Systemanforderungen](#systemanforderungen)
- [Installation](#installation)
- [Ausführung](#ausführung)
- [Architektur](#architektur)
- [Simulationsflow](#simulationsflow)


## Ziel und Zweck

Ziel des Projekts ist es, realistische Besucherdynamiken wie Bewegung, Zonenwahl (z. B. Pommesbude) und 
Panik- oder Evakuationsverhalten zu modellieren und visuell im Grid darzustellen.

### Anwendungsbereiche

- Veranstaltungsplanung und -optimierung
- Sicherheitskonzepte für Events
- Ausbildung von Security- und Rettungspersonal
- Forschung zu Crowd-Verhalten und Panik-Dynamiken

## Features

### Agenten-Verhalten

- **Besucher (Visitors)** – Autonome Bewegung zwischen verschiedenen Zonen mit realistischen Bedürfnissen
- **Security-Personal** – Reagiert auf Störungen und sichert Bereiche ab
- **Sanitäter (Medics)** – Eilt zu Notfällen und leistet Erste Hilfe
- **Feuerwehr** – Automatische Alarmierung und Anfahrt bei Bränden

### State Pattern Implementation

- `RoamingState` – Zufällige Bewegung und Zonenwahl
- `HungryThirstyState` – Nahrungssuche mit Warteschlangen
- `WCState` – Toilettengang mit Wartezeiten
- `WatchingMainActState` / `WatchingSideActState` – Bühnenprogramm verfolgen
- `QueueingState` – Intelligentes Warteschlangenverhalten mit Geduld-Mechanismus
- `PanicRunState` – Fluchtverhalten zu Notausgängen
- `EmergencyState` – Spezialisiertes Verhalten für Einsatzkräfte

### Zonen-System

- **Food Zone** – Imbissstand mit begrenzter Kapazität
- **WC Zone** – Sanitäranlagen mit Warteschlangen
- **Main Act Zone** – Hauptbühne für Konzerte
- **Side Act Zone** – Nebenbühne für kleinere Acts
- **Exit Zone** – Normaler Ausgang
- **Emergency Exit Zones** – Notausgänge (Nord, Ost, West)
- **Emergency Routes** – Fluchtrouten mit visueller Kennzeichnung

### Störungs-Management

- **Feuer (Fire)** – Automatische Alarmierung, Feuerwehr-Dispatch, Panik-Radius
- **Schlägerei (Fight)** – Security-Einsatz, Medic-Support, Bereichsabsperrung
- **Sturm (Storm)** – Evakuierung aller Besucher

### Audio-System

- Feueralarm mit Endlos-Schleife
- Feuerwehr-Sirenen während der Anfahrt
- Sturm-Warnsirenen
- Automatisches Sound-Management mit Cleanup

### Sperrzonen (Restricted Areas)

- Dynamische Erstellung um Störungen
- Automatische Umleitung für normale Besucher
- Zugangsrechte für Einsatzkräfte
- Visuelle Darstellung als rote Kreise

### Metriken & Analytics

- Zonen-Betretungen und Aufenthaltsdauern
- Warteschlangenzeiten nach Zonen
- Panik-Episoden mit Fluchtzeiten
- Event-Triggering Statistiken
- Automatischer Bericht am Simulationsende
---

## Projektstruktur

```plaintext
eventsteuerung/
├── lib/
│   └── mason.jar                     # MASON Framework (manuell hinzuzufügen)
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── events/               # Störungs-Management
│   │   │   │   ├── Disturbance.java
│   │   │   │   ├── FireDisturbance.java
│   │   │   │   ├── FightDisturbance.java
│   │   │   │   └── StormDisturbance.java
│   │   │   ├── metrics/              # Datensammlung & Analytics
│   │   │   │   ├── MetricsCollector.java
│   │   │   │   ├── DefaultMetricsCollector.java
│   │   │   │   └── MetricsViewer.java
│   │   │   ├── org.simulation/       # Haupt-Simulationslogik
│   │   │   │   ├── Agent.java        # Basis-Agent Implementierung
│   │   │   │   ├── Person.java       # Erweiterte Personen-Klasse
│   │   │   │   ├── Event.java        # Zentrale Simulations-Engine
│   │   │   │   ├── EventUI.java      # GUI & Visualisierung
│   │   │   │   ├── FireStation.java  # Feuerwache
│   │   │   │   ├── FireTruck.java    # Feuerwehrauto
│   │   │   │   ├── RestrictedArea.java
│   │   │   │   └── utils/
│   │   │   │       └── MovementUtils.java
│   │   │   ├── sounds/               # Audio-System
│   │   │   │   ├── EventSoundSystem.java
│   │   │   │   ├── AudioPlayer.java
│   │   │   │   └── SoundType.java
│   │   │   ├── states/               # State Pattern Implementation
│   │   │   │   ├── IStates.java
│   │   │   │   ├── RoamingState.java
│   │   │   │   ├── HungryThirstyState.java
│   │   │   │   ├── WCState.java
│   │   │   │   ├── WatchingMainActState.java
│   │   │   │   ├── WatchingSideActState.java
│   │   │   │   ├── QueueingState.java
│   │   │   │   ├── PanicRunState.java
│   │   │   │   └── EmergencyState.java
│   │   │   └── zones/                # Zonen-Management
│   │   │       ├── Zone.java
│   │   │       ├── FoodZone.java
│   │   │       ├── WCZone.java
│   │   │       ├── MainActZone.java
│   │   │       ├── SideActZone.java
│   │   │       ├── FireStation.java
│   │   │       └── Emergency*.java
│   │   └── resources/
│   │       ├── images/               # UI Icons & Hintergründe
│   │       │   ├── barrier.png       # Exit-Symbol
│   │       │   ├── emergency-exit.png
│   │       │   ├── EmergencyRoute*.png
│   │       │   ├── fire-station.png
│   │       │   ├── fire-truck.png
│   │       │   ├── Hintergrundbild3.png
│   │       │   ├── imbiss-stand.png
│   │       │   ├── MainAct.png
│   │       │   ├── SideAct.png
│   │       │   └── wc2.png
│   │       └── sounds/               # Audio-Dateien
│   │           ├── fire_alarm.wav
│   │           ├── fire_truck_siren.wav
│   │           └── storm_warning.wav
│   └── test/
│       └── java/                     # JUnit 5 Tests
├── pom.xml                          # Maven Konfiguration
└── README.md
```

## Systemanforderungen

- **Java** – Version 16 oder höher
- **Maven** – 3.6+ für Dependency Management
- **Memory** – Mindestens 1GB RAM
- **Audio** – Funktionsfähiges Audio-System für Sound-Features

## Installation

## 1. Repository klonen

```bash
git clone <repository-url>
cd eventsteuerung
```

### 2. MASON Framework hinzufügen

Die MASON-Bibliothek muss manuell in das `lib/` Verzeichnis eingefügt werden:

- MASON JAR herunterladen von: [https://cs.gmu.edu/\~eclab/projects/mason/](https://cs.gmu.edu/~eclab/projects/mason/)
- Datei als `mason.jar` in `lib/` platzieren

```bash
mkdir -p lib
# mason.jar in lib/ kopieren
```

### 3. Dependencies installieren

```bash
mvn clean install
```

### 4. Tests ausführen

```bash
mvn test
```

## Ausführung

### Über Maven

```bash
mvn exec:java
```

### Über IDE

- Hauptklasse: `org.simulation.EventUI`

## Simulation starten

1. **Konfiguration** – Anzahl Besucher, Sanitäter und Security festlegen
2. **Start** – Simulation startet automatisch

### Interaktion

- **Fire Button**: Feuer auslösen
- **Fight Button**: Schlägerei simulieren
- **Storm Button**: Sturm-Warnung aktivieren

### Beobachtung

- Agenten-Verhalten in Echtzeit verfolgen

### Auswertung

- Automatischer Metriken-Report am Ende

## GUI-Elemente

- **Hauptfenster** – Grid-basierte Simulation mit Zoom-Funktionalität
- **Control Panel** – Event-Trigger Buttons
- **Legende** – Farb- und Symbol-Erklärung
- **Hintergrund** – Veranstaltungsgelände-Karte

## Architektur

### Design Patterns

- **State Pattern** – Agenten-Verhalten durch austauschbare Zustände
- **Observer Pattern** – Event-System für Störungen
- **Strategy Pattern** – Verschiedene Bewegungsalgorithmen
- **Factory Pattern** – Zone- und Agent-Erstellung
- **Singleton Pattern** – Sound-System und Metriken-Sammler

## Simulationsflow

1. **Initialisierung** – Grid, Zonen und Agenten erstellen
2. **Scheduling** – MASON Scheduler für zeitbasierte Events
3. **State Machine** – Jeder Agent führt seinen aktuellen Zustand aus
4. **Event Handling** – Störungen lösen Zustandsänderungen aus
5. **Movement** – Pfadfindung und Kollisionsvermeidung
6. **Metrics** – Kontinuierliche Datensammlung
7. **Cleanup** – Ressourcen-Management und Berichte

