# Event Simulation – Agentenbasierte Simulation mit MASON

Dieses Java-Projekt simuliert das Verhalten von Besuchern auf einem Event mithilfe des agentenbasierten
Frameworks [MASON](https://cs.gmu.edu/~eclab/projects/mason/).

## Ziel

Ziel des Projekts ist es, realistische Besucherdynamiken wie Bewegung, Zonenwahl (z. B. Pommesbude), und später auch
Panik- oder Evakuationsverhalten zu modellieren und visuell im Grid darzustellen.

---

## Projektstruktur

```plaintext
eventsteuerung/                         
├── lib/                                      # Externe Bibliotheken (z. B. mason.jar)
├── src/                                      # Quellcodebasis
│   ├── main/                                 # Hauptprogrammcode
│   │   └── java/                             # Java-Paketstruktur
│   │       ├── org.simulation/               # Zentrale Komponenten der Simulation
│   │       │   ├── Agent.java                # Besucher-Logik & Verhalten
│   │       │   ├── Event.java                # Simulationseinheit mit Grid und Scheduler
│   │       │   ├── EventUI.java              # GUI-Ansicht mit MASON Display2D
│   │       │   ├── FoodZone.java             # Zone für Nahrungsbedürfnisse
│   │       │   ├── MainActZone.java          # Hauptunterhaltungszone
│   │       │   ├── Person.java               # Erweiterte Rollen (z. B. Security)
│   │       │   ├── SideActZone.java          # Nebenunterhaltungszone
│   │       │   ├── WCZone.java               # Sanitärzone
│   │       │   └── Zone.java                 # Oberklasse aller Zonentypen
│   │       └── states/                       # Zustände (State Pattern) für Agenten
│   │           ├── EmergencyState.java       # Verhalten für Nothelfer
│   │           ├── HungryThirstyState.java   # Zustand bei Hunger oder Durst
│   │           ├── IStates.java              # Interface für Zustandslogik
│   │           ├── PanicRunState.java        # Panik-Fluchtverhalten
│   │           ├── QueueingState.java        # Warteschlangenverhalten
│   │           ├── RoamingState.java         # Ziellose Bewegung
│   │           ├── SeekingZoneState.java     # Auf dem Weg zur Zielzone
│   │           └── WatchingActState.java     # Beobachtung eines Programms
│   └── test/                                 # JUnit-Tests
└── pom.xml                                   # Maven-Konfiguration
```

## Entwicklungsstand Sprint 1

### Funktionen

- Agenten bewegen sich zufällig auf dem Grid (Random-Walk)
- Zonenarten erstellt und im Grid positioniert (z. B. FoodZone, ActZone, WCZone)
- Agenten können Zielzonen ansteuern (zielgerichtetes Verhalten)
- State Pattern für Agentenlogik eingeführt:
    - z. B. `RoamingState`, `HungryThirstyState`, `QueueingState`, `PanicRunState`

### GUI (MASON Display2D)

- Darstellung der Agenten und Zonen im Grid mit Display2D
- Farblich unterscheidbare Agenten

### Architektur & Struktur

- Maven-Projekt mit MASON erfolgreich eingerichtet
- Modularisierung in:
    - `Agent`, `Zone`, `Event`, `Person`
    - Paketstruktur für states und Services