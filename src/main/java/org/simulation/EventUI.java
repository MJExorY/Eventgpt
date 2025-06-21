package org.simulation;

import sim.display.*;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.SimplePortrayal2D;
import sim.portrayal.grid.SparseGridPortrayal2D;
import sim.portrayal.simple.*;
import sim.engine.*;


import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Random;


public class EventUI extends GUIState {
    public Display2D display;
    public JFrame frame;
    public SparseGridPortrayal2D gridPortrayal = new SparseGridPortrayal2D();
    BufferedImage backgroundImage;

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

        // Erst alle Portrayals löschen
        gridPortrayal.setPortrayalForAll(null);

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

        // Person portrayal als QUADRATE mit verschiedenen Farben je nach Typ
        gridPortrayal.setPortrayalForClass(Person.class, new SimplePortrayal2D() {
            @Override
            public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
                Person person = (Person) object;
                graphics.setColor(person.getColor());

                // Quadrat zeichnen
                int size = (int) Math.min(info.draw.width, info.draw.height);
                int x = (int) (info.draw.x - size / 2);
                int y = (int) (info.draw.y - size / 2);

                graphics.fillRect(x, y, size, size);

                // Schwarzer Rand für bessere Sichtbarkeit
                graphics.setColor(Color.BLACK);
                graphics.drawRect(x, y, size, size);
            }
        });


        //Zones mit Icon
        gridPortrayal.setPortrayalForClass(Zone.class, new RectanglePortrayal2D() {

            final Image foodIcon;
            final Image mainActIcon;
            final Image exitIcon;
            final Image emergencyExitIcon;
            final Image sideActIcon;
            final Image wcIcon;

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
                // WC-Zone Icon (60x60)
                URL wcURL = getClass().getResource("/wc2.png");
                System.out.println("WC Icon URL: " + wcURL);
                if (wcURL != null) {
                    wcIcon = new ImageIcon(wcURL).getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
                } else {
                    System.err.println("❌ Bild nicht gefunden: /wc2.png");
                    wcIcon = null;
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
                //Emergency Exit Icon
                URL emergencyExitURL = getClass().getResource("/emergency-exit.png");
                System.out.println("Emergency Exit Icon URL: " + emergencyExitURL);
                if (emergencyExitURL != null) {
                    emergencyExitIcon = new ImageIcon(emergencyExitURL).getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
                } else {
                    System.err.println("❌ Bild nicht gefunden: /emergency-exit.png");
                    emergencyExitIcon = null;
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
                    graphics.drawImage(foodIcon, (int) (x - 30), (int) (y - 30), 60, 60, null);
                } else if (zone.getType() == Zone.ZoneType.ACT_MAIN && mainActIcon != null) {
                    graphics.drawImage(mainActIcon, (int) (x - 40), (int) (y - 40), 80, 80, null);
                } else if (zone.getType() == Zone.ZoneType.EXIT && exitIcon != null) {
                    graphics.drawImage(exitIcon, (int) (x - 30), (int) (y - 30), 60, 60, null);
                } else if (zone.getType() == Zone.ZoneType.EMERGENCY_EXIT && emergencyExitIcon != null) {
                    graphics.drawImage(emergencyExitIcon, (int) (x - 25), (int) (y - 25), 50, 50, null);
                } else if (zone.getType() == Zone.ZoneType.ACT_SIDE && sideActIcon != null) {
                    graphics.drawImage(sideActIcon, (int) (x - 30), (int) (y - 30), 60, 60, null);
                } else if (zone.getType() == Zone.ZoneType.WC && wcIcon != null) {
                    graphics.drawImage(wcIcon, (int) (x - 30), (int) (y - 30), 60, 60, null);
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
                g.setFont(new Font("Segoe UI Emoji", Font.PLAIN, (int) (info.draw.width * 2.5)));
                g.drawString("🔥", (float) (info.draw.x - info.draw.width / 2),
                        (float) (info.draw.y + info.draw.height / 3));
            }
        });

        gridPortrayal.setPortrayalForClass(FightDisturbance.class, new SimplePortrayal2D() {
            @Override
            public void draw(Object object, Graphics2D g, DrawInfo2D info) {
                g.setFont(new Font("Segoe UI Emoji", Font.PLAIN, (int) (info.draw.width * 2.5)));
                g.drawString("🥊", (float) (info.draw.x - info.draw.width / 2),
                        (float) (info.draw.y + info.draw.height / 3));
            }
        });

        gridPortrayal.setPortrayalForClass(StormDisturbance.class, new SimplePortrayal2D() {
            @Override
            public void draw(Object object, Graphics2D g, DrawInfo2D info) {
                g.setFont(new Font("Segoe UI Emoji", Font.PLAIN, (int) (info.draw.width * 4)));
                g.drawString("🌩️", (float) (info.draw.x - info.draw.width / 2),
                        (float) (info.draw.y + info.draw.height / 3));
            }
        });
        // FireStation
        gridPortrayal.setPortrayalForClass(FireStation.class, new SimplePortrayal2D() {
            final Image fireStationIcon;

            {
                URL iconURL = getClass().getResource("/fire-station.png");
                if (iconURL != null) {
                    fireStationIcon = new ImageIcon(iconURL).getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
                } else {
                    System.err.println("Fire Station Icon nicht gefunden: /fire-station.png");
                    fireStationIcon = null;
                }
            }

            @Override
            public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
                if (fireStationIcon != null) {
                    int x = (int) (info.draw.x - 15);
                    int y = (int) (info.draw.y - 15);
                    graphics.drawImage(fireStationIcon, x, y, 50, 50, null);
                } else {
                    // Fallback: Blaues Rechteck
                    graphics.setColor(Color.BLUE);
                    int size = (int) (info.draw.width * 2);
                    int x = (int) (info.draw.x - size / 2);
                    int y = (int) (info.draw.y - size / 2);
                    graphics.fillRect(x, y, size, size);
                }
            }
        });
        // FireTruck
        gridPortrayal.setPortrayalForClass(FireTruck.class, new SimplePortrayal2D() {
            final Image fireTruckIcon;

            {
                URL iconURL = getClass().getResource("/fire-truck.png");
                if (iconURL != null) {
                    fireTruckIcon = new ImageIcon(iconURL).getImage().getScaledInstance(40, 25, Image.SCALE_SMOOTH);
                } else {
                    System.err.println("Fire Truck Icon nicht gefunden: /fire-truck.png");
                    fireTruckIcon = null;
                }
            }

            @Override
            public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
                FireTruck truck = (FireTruck) object;

                if (fireTruckIcon != null) {
                    int x = (int) (info.draw.x - 20);
                    int y = (int) (info.draw.y - 13);
                    graphics.drawImage(fireTruckIcon, x, y, 40, 25, null);
                } else {
                    // Fallback: Rotes Rechteck
                    graphics.setColor(truck.getColor());
                    int width = (int) (info.draw.width * 3);
                    int height = (int) (info.draw.height * 2);
                    int x = (int) (info.draw.x - width / 2);
                    int y = (int) (info.draw.y - height / 2);
                    graphics.fillRect(x, y, width, height);
                }

                // Bewegungsrichtung anzeigen
                if (truck.isMoving()) {
                    graphics.setColor(Color.YELLOW);
                    graphics.drawOval((int) (info.draw.x - 35), (int) (info.draw.y - 25), 70, 50);
                }
            }
        });

        display.reset();
        //  display.setBackdrop(new Color(0xE1CAB2));
        display.repaint();
    }

    @Override
    public void init(sim.display.Controller c) {
        super.init(c);


        //Hintergrund map setzen-
        display = new Display2D(650, 650, this);
        display.setClipping(false);
        display.setBackdrop(Color.WHITE);
        URL backgroundURL = getClass().getResource("/Hintergrundbild.png");
        if (backgroundURL != null) {
            try {
                BufferedImage bgImage = ImageIO.read(backgroundURL);
                Rectangle anchor = new Rectangle(0, 0, bgImage.getWidth(), bgImage.getHeight());
                TexturePaint texturePaint = new TexturePaint(bgImage, anchor);
                display.setBackdrop(texturePaint);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("❌ Hintergrundbild nicht gefunden: /Hintergrundbild.png");
        }
        //---


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
        JPanel legendPanel = new JPanel(new GridBagLayout());
        legendPanel.setOpaque(true);
        legendPanel.setBackground(new Color(0, 0, 0, 160));
        legendPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(2, 0, 2, 120);

        //SPalte Rollen
        gbc.gridx = 0;
        gbc.gridy = 0;
        legendPanel.add(createSectionTitle("Roles"), gbc);
        gbc.gridy++;
        legendPanel.add(createCompactLegendEntry("●", Color.YELLOW, "Visitor"), gbc);
        gbc.gridy++;
        legendPanel.add(createCompactLegendEntry("■", Color.WHITE, "Medic"), gbc);
        gbc.gridy++;
        legendPanel.add(createCompactLegendEntry("■", Color.DARK_GRAY, "Security"), gbc);
        gbc.gridy++;
        legendPanel.add(createCompactLegendEntry("🚒", Color.RED, "Fire Truck"), gbc);

        // Spalte 2: Zustände
        gbc.gridx = 1;
        gbc.gridy = 0;
        legendPanel.add(createSectionTitle("States"), gbc);
        gbc.gridy++;
        legendPanel.add(createCompactLegendEntry("●", Color.GREEN, "Eating"), gbc);
        gbc.gridy++;
        legendPanel.add(createCompactLegendEntry("●", Color.MAGENTA, "Seeking"), gbc);
        gbc.gridy++;
        legendPanel.add(createCompactLegendEntry("●", Color.BLUE, "Watching"), gbc);
        gbc.gridy++;
        legendPanel.add(createCompactLegendEntry("●", Color.RED, "Panic"), gbc);
        gbc.gridy++;
        legendPanel.add(createCompactLegendEntry("●", Color.ORANGE, "Queue"), gbc);
        gbc.gridy++;
        legendPanel.add(createCompactLegendEntry("●", Color.PINK, "Using WC"), gbc);


        // Spalte 3: Zonen
        gbc.gridx = 2;
        gbc.gridy = 0;
        legendPanel.add(createSectionTitle("Zones"), gbc);
        gbc.gridy++;
        legendPanel.add(createIconLegendEntry(scaledIcon("/imbiss-stand.png", 30, 30), "Food"), gbc);
        gbc.gridy++;
        legendPanel.add(createIconLegendEntry(scaledIcon("/MainAct.png", 30, 30), "Main Stage"), gbc);
        gbc.gridy++;
        legendPanel.add(createIconLegendEntry(scaledIcon("/SideAct.png", 30, 30), "Side Stage"), gbc);
        gbc.gridy++;
        legendPanel.add(createIconLegendEntry(scaledIcon("/barrier.png", 30, 30), "Exit"), gbc);
        gbc.gridy++;
        legendPanel.add(createIconLegendEntry(scaledIcon("/emergency-exit.png", 25, 25), "Emergency"), gbc);
        gbc.gridy++;
        legendPanel.add(createIconLegendEntry(scaledIcon("/wc2.png", 30, 30), "WC"), gbc);

        // Wrapper Panel für Positionierung links unten
        JPanel legendWrapper = new JPanel(new BorderLayout());
        legendWrapper.setOpaque(false);
        legendWrapper.add(legendPanel, BorderLayout.WEST);

        frame.getContentPane().add(legendWrapper, BorderLayout.SOUTH);
    }

    private JLabel createSectionTitle(String title) {
        JLabel label = new JLabel(title);
        label.setFont(new Font("Dialog", Font.BOLD, 14));
        label.setForeground(Color.WHITE);
        return label;
    }

    private JPanel createCompactLegendEntry(String symbol, Color color, String text) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panel.setOpaque(false);

        JLabel symbolLabel = new JLabel(symbol);
        if (color != null) {
            symbolLabel.setForeground(color);
        }
        symbolLabel.setFont(new Font("Dialog", Font.PLAIN, 13));

        JLabel textLabel = new JLabel(text);
        textLabel.setFont(new Font("Dialog", Font.PLAIN, 13));
        textLabel.setForeground(Color.WHITE);

        panel.add(symbolLabel);
        panel.add(textLabel);
        return panel;
    }

    private JPanel createIconLegendEntry(ImageIcon icon, String text) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panel.setOpaque(false);

        JLabel iconLabel = new JLabel(icon);
        JLabel textLabel = new JLabel(text);
        textLabel.setFont(new Font("Dialog", Font.PLAIN, 13));
        textLabel.setForeground(Color.WHITE);

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
