# Event Simulation – Agentenbasierte Simulation mit MASON

Dieses Java-Projekt simuliert das Verhalten von Besuchern auf einem Event mithilfe des agentenbasierten Frameworks [MASON](https://cs.gmu.edu/~eclab/projects/mason/).

## Ziel

Ziel des Projekts ist es, realistische Besucherdynamiken wie Bewegung, Zonenwahl (z. B. Pommesbude), und später auch Panik- oder Evakuationsverhalten zu modellieren und visuell im Grid darzustellen.

---

## Projektstruktur

```plaintext
src/
├── main/
│   └── java/
│       └── org.simulation/
│           ├── Event.java          # Hauptsimulation (SimState)
│           ├── EventUI.java       # GUI-Controller (GUIState)
│           ├── Agent.java         # Agent (z. B. Festivalbesucher)
│           └── ...
├── lib/
│   └── mason.jar                  # lokal eingebundene MASON-Bibliothek
└── pom.xml                        # Maven-Konfiguration


## Entwicklungsstand – Sprint 1

### Funktionen

- Simulation von Agenten auf einem Grid (MASON-basiert)
- Agenten bewegen sich zufällig über angrenzende Felder (Random-Walk)
- Positionierung und Anzahl von Agenten werden beim Start dynamisch gesetzt

### GUI (MASON Display2D)

- Darstellung der Agenten im Grid mit "Display2D"
- Farblich unterscheidbare Agenten (z. B. Schwarz = beweglich, Rot = statisch)


### Architektur & Vorbereitung

- Maven-Projekt mit MASON erfolgreich eingerichtet
- "Agent"-Klasse übernimmt derzeit das Steppable-Verhalten für Besucher
- "Person"-Klasse als Vorbereitung für Zustandslogik erstellt
- Vorbereitung des State Patterns gestartet (z. B. Wandering/Seeking State)


