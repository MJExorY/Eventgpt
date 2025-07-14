package states;

import org.simulation.Agent;
import org.simulation.Event;

/**
 * Das IStates-Interface definiert das Verhalten eines Agenten
 * innerhalb eines bestimmten Zustands im Zustandsautomaten.
 * Jeder Zustand muss die {@code act}-Methode implementieren,
 * die bei jedem Zeitschritt aufgerufen wird.
 * Zustände steuern z. B. Bewegung, Interaktion mit Zonen
 * oder Reaktion auf Ereignisse wie Panik oder Notfälle.
 *
 * @author Burak Tamer
 */

public interface IStates {
    IStates act(Agent g, Event event);

}
