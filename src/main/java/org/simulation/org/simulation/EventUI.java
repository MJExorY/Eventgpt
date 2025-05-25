package org.simulation;

import sim.display.*;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.grid.SparseGridPortrayal2D;
import sim.portrayal.simple.*;
import sim.engine.*;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class EventUI extends GUIState {
    public Display2D display;
    public JFrame frame;
    public SparseGridPortrayal2D gridPortrayal = new SparseGridPortrayal2D();

    public EventUI(SimState state) {
        super(state);
    }

    public static void main(String[] args) {
        Random rand = new Random();
        int agentCount = rand.nextInt(1000) + 1;
        Event sim = new Event(System.currentTimeMillis(), agentCount);
        EventUI gui = new EventUI(sim);
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

        // Agenten einfärben über getColor()
        gridPortrayal.setPortrayalForClass(Agent.class, new OvalPortrayal2D() {
            @Override
            public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
                Agent agent = (Agent) object;
                graphics.setColor(agent.getColor());
                graphics.fillOval((int) (info.draw.x - info.draw.width / 2),
                        (int) (info.draw.y - info.draw.height / 2),
                        (int) (info.draw.width),
                        (int) (info.draw.height));
            }
        });

        // Personen (Sanitäter) in rot
        gridPortrayal.setPortrayalForClass(Person.class,
                new OvalPortrayal2D(Color.RED));

        // Zonen
        gridPortrayal.setPortrayalForClass(Zone.class, new RectanglePortrayal2D() {
            @Override
            public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
                Zone zone = (Zone) object;
                Color color;

                switch (zone.getType()) {
                    case FOOD -> color = Color.GREEN;
                    case ACT_MAIN -> color = Color.BLUE;
                    case ACT_SIDE -> color = Color.CYAN;
                    case EXIT -> color = Color.DARK_GRAY;
                    default -> color = Color.GRAY;
                }

                graphics.setColor(color);

                double scale = 1.8;
                double width = info.draw.width * scale;
                double height = info.draw.height * scale;
                double x = info.draw.x - width / 2;
                double y = info.draw.y - height / 2;

                graphics.fillRect((int) x, (int) y, (int) width, (int) height);
            }
        });

        display.reset();
        display.setBackdrop(Color.WHITE);
        display.repaint();
    }

    public void init(sim.display.Controller c) {
        super.init(c);

        display = new Display2D(600, 600, this);
        display.setClipping(false);
        frame = display.createFrame();
        c.registerFrame(frame);
        frame.setVisible(true);

        display.attach(gridPortrayal, "Event Grid");

        // Legende
        JPanel legendPanel = new JPanel();
        legendPanel.setLayout(new BoxLayout(legendPanel, BoxLayout.Y_AXIS));
        legendPanel.setOpaque(false);

        JLabel roleTitle = new JLabel("Roles:");
        roleTitle.setFont(new Font("Dialog", Font.BOLD, 13));
        legendPanel.add(roleTitle);
        legendPanel.add(createLegendEntry(Color.YELLOW, "● Visitor (Roaming)"));
        legendPanel.add(createLegendEntry(Color.RED, "● Person"));

        JLabel stateTitle = new JLabel("States:");
        stateTitle.setFont(new Font("Dialog", Font.BOLD, 13));
        legendPanel.add(Box.createVerticalStrut(5));
        legendPanel.add(stateTitle);
        legendPanel.add(createLegendEntry(Color.GREEN, "● Eating"));
        legendPanel.add(createLegendEntry(Color.MAGENTA, "● Seeking"));
        legendPanel.add(createLegendEntry(Color.BLUE, "● Watching Act"));
        legendPanel.add(createLegendEntry(Color.ORANGE, "● Panic Run"));
        legendPanel.add(createLegendEntry(Color.ORANGE, "● In Queue"));

        JLabel zoneTitle = new JLabel("Zones:");
        zoneTitle.setFont(new Font("Dialog", Font.BOLD, 13));
        legendPanel.add(Box.createVerticalStrut(5));
        legendPanel.add(zoneTitle);
        legendPanel.add(createLegendEntry(Color.GREEN, "■ FoodZone"));
        legendPanel.add(createLegendEntry(Color.BLUE, "■ Main Stage"));
        legendPanel.add(createLegendEntry(Color.CYAN, "■ Side Stage"));
        legendPanel.add(createLegendEntry(Color.PINK, "■ Exit"));

        frame.getContentPane().add(legendPanel, BorderLayout.SOUTH);
    }

    private JPanel createLegendEntry(Color color, String label) {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panel.setOpaque(false);

        JLabel symbolLabel = new JLabel(label.substring(0, 1));
        symbolLabel.setForeground(color);
        symbolLabel.setFont(new Font("Dialog", Font.PLAIN, 13));

        JLabel textLabel = new JLabel(label.substring(2));
        textLabel.setFont(new Font("Dialog", Font.PLAIN, 12));

        panel.add(symbolLabel);
        panel.add(textLabel);
        return panel;
    }

    public void quit() {
        super.quit();
        if (frame != null) frame.dispose();
        frame = null;
        display = null;
    }
}
