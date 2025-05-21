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
