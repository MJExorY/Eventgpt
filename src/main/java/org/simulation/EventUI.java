package org.simulation;

import sim.engine.SimState;
import sim.display.Console;
import sim.display.GUIState;
import sim.display.Display2D;
import sim.portrayal.grid.SparseGridPortrayal2D;

import javax.swing.*;
import java.util.Random;

public class EventUI extends GUIState {
    public Display2D display;
    public JFrame frame;
    public SparseGridPortrayal2D gridPortrayal = new SparseGridPortrayal2D();

    public EventUI(SimState state) {
        super(state);
    }

    public static void main(String[] args) {

        //Main-Methode für die Agents
        Random rand = new Random();
        int agentCount = rand.nextInt(1000) + 1; // Zahl zwischen 1 und 1000
        Event sim = new Event(System.currentTimeMillis(), agentCount); // einmalige Erstellung!

        EventUI gui = new EventUI(sim); // selbe Instanz übergeben!
        Console console = new Console(gui);
        console.setVisible(true);

    }

    public void start() {
        super.start();
        setupPortrayals();
    }

    public void load(SimState state) {
        super.load(state);
        setupPortrayals();
    }

    public void setupPortrayals() {
        Event sim = (Event) state;

        gridPortrayal.setField(sim.grid);

        // Darstellung für Agent hinzufügen:
        gridPortrayal.setPortrayalForClass(Agent.class,
                new sim.portrayal.simple.OvalPortrayal2D(java.awt.Color.BLACK));

        display.reset();
        display.setBackdrop(java.awt.Color.white);
        display.repaint();

        //10 Personen, in der Farbe rot
        gridPortrayal.setPortrayalForClass(Person.class,
                new sim.portrayal.simple.OvalPortrayal2D(java.awt.Color.RED));

        //Agenten visuell darstellen
        gridPortrayal.setPortrayalForClass(Agent.class,
                new sim.portrayal.simple.OvalPortrayal2D(java.awt.Color.BLACK));

    }


    public void init(sim.display.Controller c) {
        super.init(c);

        display = new Display2D(600, 600, this);
        display.setClipping(false);
        frame = display.createFrame();
        c.registerFrame(frame);
        frame.setVisible(true);

        display.attach(gridPortrayal, "Event Grid");
    }

    public void quit() {
        super.quit();
        if (frame != null) frame.dispose();
        frame = null;
        display = null;
    }


}
