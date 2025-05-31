package org.simulation;

import sim.display.*;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.SimplePortrayal2D;
import sim.portrayal.grid.SparseGridPortrayal2D;
import sim.portrayal.simple.*;
import sim.engine.*;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
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

        // Agent color via getColor()
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

        // Persons (e.g. medics) in red
        gridPortrayal.setPortrayalForClass(Person.class,
                new OvalPortrayal2D(Color.RED));

        // Zones
     /**   gridPortrayal.setPortrayalForClass(Zone.class, new RectanglePortrayal2D() {
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
        */

        gridPortrayal.setPortrayalForClass(Zone.class, new RectanglePortrayal2D() {

            final Image foodIcon;
            final Image mainActIcon;
            final Image exitIcon;
            final Image sideActIcon;

            {
                // FOOD-Zone Icon (60x60)
                URL foodURL = getClass().getResource("/imbiss-stand.png");
                System.out.println("Food Icon URL: " + foodURL);
                if (foodURL != null) {
                    foodIcon = new ImageIcon(foodURL).getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
                } else {
                    System.err.println("❌ Bild nicht gefunden: /imbiss-stand.png");
                    foodIcon = null;
                }

                // ACT_MAIN-Zone Icon (80x80)
                URL mainActURL = getClass().getResource("/MainAct.png");
                System.out.println("MainAct Icon URL: " + mainActURL);
                if (mainActURL != null) {
                    mainActIcon = new ImageIcon(mainActURL).getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
                } else {
                    System.err.println("❌ Bild nicht gefunden: /MainAct.png");
                    mainActIcon = null;
                }

                // EXIT-Zone Icon (60x60)
                URL exitURL = getClass().getResource("/barrier.png");
                System.out.println("Exit Icon URL: " + exitURL);
                if (exitURL != null) {
                    exitIcon = new ImageIcon(exitURL).getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
                } else {
                    System.err.println("❌ Bild nicht gefunden: /barrier.png");
                    exitIcon = null;
                }

                // ACT_SIDE-Zone Icon (60x60)
                URL sideActURL = getClass().getResource("/SideAct.png");
                System.out.println("SideAct Icon URL: " + sideActURL);
                if (sideActURL != null) {
                    sideActIcon = new ImageIcon(sideActURL).getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
                } else {
                    System.err.println("❌ Bild nicht gefunden: /SideAct.png");
                    sideActIcon = null;
                }
            }

            @Override
            public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
                Zone zone = (Zone) object;

                double scale = 3.0;
                double width = info.draw.width * scale;
                double height = info.draw.height * scale;
                double x = info.draw.x - width / 2;
                double y = info.draw.y - height / 2;

                if (zone.getType() == Zone.ZoneType.FOOD && foodIcon != null) {
                    graphics.drawImage(foodIcon, (int)(x - 30), (int)(y - 30), 60, 60, null);
                } else if (zone.getType() == Zone.ZoneType.ACT_MAIN && mainActIcon != null) {
                    graphics.drawImage(mainActIcon, (int)(x - 40), (int)(y - 40), 80, 80, null);
                } else if (zone.getType() == Zone.ZoneType.EXIT && exitIcon != null) {
                    graphics.drawImage(exitIcon, (int)(x - 30), (int)(y - 30), 60, 60, null);
                } else if (zone.getType() == Zone.ZoneType.ACT_SIDE && sideActIcon != null) {
                    graphics.drawImage(sideActIcon, (int)(x - 30), (int)(y - 30), 60, 60, null);
                } else {
                    // Fallback: Farbe falls Icon fehlt
                    graphics.setColor(Color.GRAY);
                    graphics.fillRect((int) x, (int) y, (int) width, (int) height);
                }
            }
        });


        // Disturbances with emojis
        gridPortrayal.setPortrayalForClass(FireDisturbance.class, new SimplePortrayal2D() {
            @Override
            public void draw(Object object, Graphics2D g, DrawInfo2D info) {
                g.setFont(new Font("Segoe UI Emoji", Font.PLAIN, (int)(info.draw.width * 1.5)));
                g.drawString("🔥", (float)(info.draw.x - info.draw.width / 2),
                        (float)(info.draw.y + info.draw.height / 3));
            }
        });

        gridPortrayal.setPortrayalForClass(FightDisturbance.class, new SimplePortrayal2D() {
            @Override
            public void draw(Object object, Graphics2D g, DrawInfo2D info) {
                g.setFont(new Font("Segoe UI Emoji", Font.PLAIN, (int)(info.draw.width * 1.5)));
                g.drawString("🥊", (float)(info.draw.x - info.draw.width / 2),
                        (float)(info.draw.y + info.draw.height / 3));
            }
        });

        gridPortrayal.setPortrayalForClass(StormDisturbance.class, new SimplePortrayal2D() {
            @Override
            public void draw(Object object, Graphics2D g, DrawInfo2D info) {
                g.setFont(new Font("Segoe UI Emoji", Font.PLAIN, (int)(info.draw.width * 1.5)));
                g.drawString("🌩️", (float)(info.draw.x - info.draw.width / 2),
                        (float)(info.draw.y + info.draw.height / 3));
            }
        });

        display.reset();
        display.setBackdrop(new Color(0xE1CAB2));
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


        // Reference to the simulation
        Event event = (Event) state;

        // Panel for buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());

        // Fire Button
        JButton fireButton = new JButton("🔥 Fire");
        fireButton.addActionListener(e -> {
            event.spawn(FireDisturbance.createRandom(event));
        });

        // Fight Button
        JButton fightButton = new JButton("🥊 Fight");
        fightButton.addActionListener(e -> {
            event.spawn(FightDisturbance.createRandom(event));
        });

        // Storm Button
        JButton stormButton = new JButton("🌩️ Storm");
        stormButton.addActionListener(e -> {
            event.spawn(StormDisturbance.createRandom(event));
        });

        // Add buttons to panel
        buttonPanel.add(fireButton);
        buttonPanel.add(fightButton);
        buttonPanel.add(stormButton);

        // Add panel to top of window
        frame.getContentPane().add(buttonPanel, BorderLayout.NORTH);


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
       //legendPanel.add(createLegendEntry(Color.GREEN, "■ FoodZone"));
        //legendPanel.add(createLegendEntry(new ImageIcon(getClass().getResource("/imbiss-stand.png")), "Food Zone"));
        legendPanel.add(createLegendEntry(scaledIcon("/imbiss-stand.png", 10, 10), "Food Zone"));
        legendPanel.add(createLegendEntry(scaledIcon("/MainAct.png", 10, 10), "Main Stage"));
        legendPanel.add(createLegendEntry(scaledIcon("/SideAct.png", 10, 10), "Side Stage"));
        legendPanel.add(createLegendEntry(scaledIcon("/barrier.png", 10, 10), "Exit"));


       // legendPanel.add(createLegendEntry(Color.BLUE, "■ Main Stage"));
       // legendPanel.add(createLegendEntry(Color.CYAN, "■ Side Stage"));
        // legendPanel.add(createLegendEntry(Color.PINK, "■ Exit"));

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

    private JPanel createLegendEntry(ImageIcon icon, String label) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panel.setOpaque(false);

        JLabel iconLabel = new JLabel(icon);
        JLabel textLabel = new JLabel(label);
        textLabel.setFont(new Font("Dialog", Font.PLAIN, 12));

        panel.add(iconLabel);
        panel.add(textLabel);
        return panel;
    }
    private ImageIcon scaledIcon(String path, int width, int height) {
        URL url = getClass().getResource(path);
        if (url != null) {
            Image img = new ImageIcon(url).getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } else {
            System.err.println("❌ Icon nicht gefunden: " + path);
            return new ImageIcon(); // leeres Icon als Fallback
        }
    }


    public void quit() {
        super.quit();
        if (frame != null) frame.dispose();
        frame = null;
        display = null;
    }
}
