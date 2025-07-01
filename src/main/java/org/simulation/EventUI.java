package org.simulation;

import events.FightDisturbance;
import events.FireDisturbance;
import events.StormDisturbance;
import metrics.DefaultMetricsCollector;
import metrics.MetricsCollector;
import metrics.MetricsViewer;
import sim.display.Console;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.SimplePortrayal2D;
import sim.portrayal.grid.SparseGridPortrayal2D;
import sim.portrayal.simple.OvalPortrayal2D;
import sim.portrayal.simple.RectanglePortrayal2D;
import sim.util.Int2D;
import zones.FireStation;
import zones.Zone;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.logging.Logger;


public class EventUI extends GUIState {
    public Display2D display;
    public JFrame frame;
    public SparseGridPortrayal2D gridPortrayal = new SparseGridPortrayal2D();
    BufferedImage backgroundImage;
    private static final Logger logger = Logger.getLogger(EventUI.class.getName());
    private static final String PATH_EMERGENCY_RECHTS = "/images/EmergencyRouteRECHTS.png";
    // Emergency route anonymous objects and visibility flag
    private final Object emergencyRouteRight = new Object();
    private final Object emergencyRouteLeft = new Object();
    private final Object emergencyRouteStraight = new Object();
    private boolean emergencyRoutesVisible = false;

    public EventUI(SimState state) {
        super(state);
    }

    public static void main(String[] args) {
        MetricsCollector collector = new DefaultMetricsCollector();
        for (Zone.ZoneType type : Zone.ZoneType.values()) {
            collector.registerMetric("ZoneEntry_" + type);
            collector.registerMetric("ZoneExit_" + type);
            collector.registerMetric("PanicEscape_" + type);
            collector.registerMetric("TimeInZone_" + type);
            collector.registerMetric("QueueWait_" + type);
            collector.registerMetric("PanicDuration");
        }
        for (String evt : List.of("FIRE", "FIGHT", "STORM")) {
            collector.registerMetric("EventTriggered_" + evt);
        }
        // Eingabedialog für Startkonfiguration
        JTextField visitorField = new JTextField("200");
        JTextField medicField = new JTextField("5");
        JTextField securityField = new JTextField("5");

        JPanel panel = new JPanel(new GridLayout(0, 2));
        panel.add(new JLabel("Anzahl Besucher:"));
        panel.add(visitorField);
        panel.add(new JLabel("Anzahl Sanitäter:"));
        panel.add(medicField);
        panel.add(new JLabel("Anzahl Sicherheitskräfte:"));
        panel.add(securityField);

        int result = JOptionPane.showConfirmDialog(null, panel, "Startkonfiguration",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                int visitors = Integer.parseInt(visitorField.getText());
                int medics = Integer.parseInt(medicField.getText());
                int security = Integer.parseInt(securityField.getText());

                Event sim = new Event(System.currentTimeMillis(), visitors, medics, security, collector);
                EventUI gui = new EventUI(sim);
                Console console = new Console(gui);
                console.setVisible(true);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Bitte gültige Zahlen eingeben.");
            }
        } else {
            System.exit(0);
        }
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
            final Image rightEmergencyRouteIcon;
            final Image leftEmergencyRouteIcon;
            final Image StraightEmergencyRouteIcon;

            {
                // FOOD-Zone Icon (60x60)
                URL foodURL = getClass().getResource("/images/imbiss-stand.png");
                System.out.println("Food Icon URL: " + foodURL);
                if (foodURL != null) {
                    foodIcon = new ImageIcon(foodURL).getImage()
                            .getScaledInstance(60, 60, Image.SCALE_SMOOTH);
                } else {
                    logger.warning("❌ Bild nicht gefunden: /images/imbiss-stand.png");
                    foodIcon = null;
                }
                // WC-Zone Icon (60x60)
                URL wcURL = getClass().getResource("/images/wc2.png");
                System.out.println("WC Icon URL: " + wcURL);
                if (wcURL != null) {
                    wcIcon = new ImageIcon(wcURL).getImage()
                            .getScaledInstance(60, 60, Image.SCALE_SMOOTH);
                } else {
                    logger.warning("❌ Bild nicht gefunden: /images/wc2.png");
                    wcIcon = null;
                }
                // ACT_MAIN-Zone Icon (80x80)
                URL mainActURL = getClass().getResource("/images/MainAct.png");
                System.out.println("MainAct Icon URL: " + mainActURL);
                if (mainActURL != null) {
                    mainActIcon = new ImageIcon(mainActURL).getImage()
                            .getScaledInstance(80, 80, Image.SCALE_SMOOTH);
                } else {
                    logger.warning("❌ Bild nicht gefunden: /images/MainAct.png");
                    mainActIcon = null;
                }

                // EXIT-Zone Icon (60x60)
                URL exitURL = getClass().getResource("/images/barrier.png");
                System.out.println("Exit Icon URL: " + exitURL);
                if (exitURL != null) {
                    exitIcon = new ImageIcon(exitURL).getImage()
                            .getScaledInstance(60, 60, Image.SCALE_SMOOTH);
                } else {
                    logger.warning("❌ Bild nicht gefunden: /images/barrier.png");
                    exitIcon = null;
                }
                //Emergency Exit Icon
                URL emergencyExitURL = getClass().getResource("/images/emergency-exit.png");
                System.out.println("Emergency Exit Icon URL: " + emergencyExitURL);
                if (emergencyExitURL != null) {
                    emergencyExitIcon = new ImageIcon(emergencyExitURL).getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
                } else {
                    logger.warning("❌ Bild nicht gefunden: /images/emergency-exit.png");
                    emergencyExitIcon = null;
                }

                // ACT_SIDE-Zone Icon (60x60)
                URL sideActURL = getClass().getResource("/images/SideAct.png");
                System.out.println("SideAct Icon URL: " + sideActURL);
                if (sideActURL != null) {
                    sideActIcon = new ImageIcon(sideActURL).getImage()
                            .getScaledInstance(60, 60, Image.SCALE_SMOOTH);
                } else {
                    logger.warning("❌ Bild nicht gefunden: /images/SideAct.png");
                    sideActIcon = null;
                }


                URL rightExitURL = getClass().getResource(PATH_EMERGENCY_RECHTS);
                System.out.println("RightExit Icon URL: " + rightExitURL);
                if (rightExitURL != null) {
                    rightEmergencyRouteIcon = new ImageIcon(rightExitURL).getImage().getScaledInstance(90, 30, Image.SCALE_SMOOTH);
                } else {
                    logger.warning("❌ Bild nicht gefunden: /images/EmergencyRouteRECHTS.png");
                    rightEmergencyRouteIcon = null;
                }

                {
                    URL iconURL = getClass().getResource("/images/EmergencyRouteLINKS.png");
                    if (iconURL != null) {
                        leftEmergencyRouteIcon = new ImageIcon(iconURL).getImage().getScaledInstance(90, 30, Image.SCALE_SMOOTH);
                    } else {
                        logger.warning("Emergency Root Links Icon nicht gefunden: /images/EmergencyRouteLINKS.png");
                        leftEmergencyRouteIcon = null;
                    }
                }
                {
                    URL iconURL = getClass().getResource("/images/EmergencyRouteSTRAIGHT.png");
                    if (iconURL != null) {
                        StraightEmergencyRouteIcon = new ImageIcon(iconURL).getImage().getScaledInstance(30, 90, Image.SCALE_SMOOTH);
                    } else {
                        System.err.println("Emergency Root Straight Icon nicht gefunden: /images/EmergencyRouteSTRAIGHT.png");
                        StraightEmergencyRouteIcon = null;
                    }
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

        // EmergencyRoute anonymous objects portrayals
        gridPortrayal.setPortrayalForObject(emergencyRouteRight, new SimplePortrayal2D() {
            final Image icon = scaledIcon("/images/EmergencyRouteRECHTS.png", 90, 30).getImage();

            @Override
            public void draw(Object o, Graphics2D g, DrawInfo2D info) {
                if (icon != null) g.drawImage(icon, (int) (info.draw.x - 45), (int) (info.draw.y - 15), null);
            }
        });
        gridPortrayal.setPortrayalForObject(emergencyRouteLeft, new SimplePortrayal2D() {
            final Image icon = scaledIcon("/images/EmergencyRouteLINKS.png", 90, 30).getImage();

            @Override
            public void draw(Object o, Graphics2D g, DrawInfo2D info) {
                if (icon != null) g.drawImage(icon, (int) (info.draw.x - 45), (int) (info.draw.y - 15), null);
            }
        });
        gridPortrayal.setPortrayalForObject(emergencyRouteStraight, new SimplePortrayal2D() {
            final Image icon = scaledIcon("/images/EmergencyRouteSTRAIGHT.png", 30, 90).getImage();

            @Override
            public void draw(Object o, Graphics2D g, DrawInfo2D info) {
                if (icon != null) g.drawImage(icon, (int) (info.draw.x - 15), (int) (info.draw.y - 45), null);
            }
        });


        // FireStation
        gridPortrayal.setPortrayalForClass(FireStation.class, new SimplePortrayal2D() {
            final Image fireStationIcon;

            {
                URL iconURL = getClass().getResource("/images/fire-station.png");
                if (iconURL != null) {
                    fireStationIcon = new ImageIcon(iconURL).getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
                } else {
                    System.err.println("Fire Station Icon nicht gefunden: /images/fire-station.png");
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
                URL iconURL = getClass().getResource("/images/fire-truck.png");
                if (iconURL != null) {
                    fireTruckIcon = new ImageIcon(iconURL).getImage().getScaledInstance(40, 25, Image.SCALE_SMOOTH);
                } else {
                    System.err.println("Fire Truck Icon nicht gefunden: /images/fire-truck.png");
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

        gridPortrayal.setPortrayalForObject(RestrictedArea.class, new SimplePortrayal2D() {
            @Override
            public void draw(Object object, Graphics2D g, DrawInfo2D info) {
                RestrictedArea ra = (RestrictedArea) object;

                int radius = ra.getRadius();
                double scale = info.draw.width; // Skalierung pro Zelle
                int drawRadius = (int) (radius * scale);
                int drawX = (int) (info.draw.x - drawRadius);
                int drawY = (int) (info.draw.y - drawRadius);

                g.setColor(new Color(255, 0, 0, 80)); // rot-transparent
                g.fillOval(drawX, drawY, 2 * drawRadius, 2 * drawRadius);
                g.setColor(Color.RED);
                g.drawOval(drawX, drawY, 2 * drawRadius, 2 * drawRadius);
            }
        });
        gridPortrayal.setPortrayalForObject(RestrictedArea.class, new SimplePortrayal2D() {
            @Override
            public void draw(Object object, Graphics2D g, DrawInfo2D info) {
                RestrictedArea ra = (RestrictedArea) object;

                int radius = ra.getRadius();
                double scale = info.draw.width; // Skalierung pro Zelle
                int drawRadius = (int) (radius * scale);
                int drawX = (int) (info.draw.x - drawRadius);
                int drawY = (int) (info.draw.y - drawRadius);

                g.setColor(new Color(255, 0, 0, 80)); // rot-transparent
                g.fillOval(drawX, drawY, 2 * drawRadius, 2 * drawRadius);
                g.setColor(Color.RED);
                g.drawOval(drawX, drawY, 2 * drawRadius, 2 * drawRadius);
            }
        });
        display.reset();
        //  display.setBackdrop(new Color(0xE1CAB2));
        display.repaint();
    }

    @Override
    public void init(Controller c) {
        super.init(c);


        //Hintergrund map setzen-

        display = new Display2D(650, 650, this);
        display.setClipping(false);
        display.setBackdrop(Color.WHITE);
        URL backgroundURL = getClass().getResource("/images/Hintergrundbild3.png");
        if (backgroundURL != null) {
            try {
                BufferedImage bgImage = ImageIO.read(backgroundURL);
                Rectangle anchor = new Rectangle(0, 0, bgImage.getWidth(), bgImage.getHeight());
                TexturePaint texturePaint = new TexturePaint(bgImage, anchor);
                display.setBackdrop(texturePaint);
            } catch (IOException e) {
                logger.severe("Fehler beim Laden des Hintergrundbilds: " + e.getMessage());
            }
        } else {
            System.err.println("❌ Hintergrundbild nicht gefunden: /Hintergrundbild.png");
        }


        frame = display.createFrame();
        c.registerFrame(frame);
        frame.setVisible(true);

        display.attach(gridPortrayal, "Event Grid");


        // Reference to the simulation
        Event event = (Event) state;
        MetricsCollector collector = event.getCollector();

        // Panel for buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());

        // Fire Button
        JButton fireButton = new JButton("🔥 Fire");
        fireButton.addActionListener(e -> {
            event.spawn(FireDisturbance.createRandom(event));
            collector.recordEventTriggered("FIRE");
            if (!emergencyRoutesVisible) {
                event.grid.setObjectLocation(emergencyRouteRight, new Int2D(83, 50));
                event.grid.setObjectLocation(emergencyRouteLeft, new Int2D(14, 50));
                event.grid.setObjectLocation(emergencyRouteStraight, new Int2D(50, 15));
                emergencyRoutesVisible = true;
                display.repaint();
            }
        });

        // Fight Button
        JButton fightButton = new JButton("🥊 Fight");
        fightButton.addActionListener(e -> {
            event.spawn(FightDisturbance.createRandom(event));
            collector.recordEventTriggered("FIGHT");
            if (!emergencyRoutesVisible) {
                event.grid.setObjectLocation(emergencyRouteRight, new Int2D(83, 50));
                event.grid.setObjectLocation(emergencyRouteLeft, new Int2D(14, 50));
                event.grid.setObjectLocation(emergencyRouteStraight, new Int2D(50, 15));
                emergencyRoutesVisible = true;
                display.repaint();
            }
        });

        // Storm Button
        JButton stormButton = new JButton("🌩️ Storm");
        stormButton.addActionListener(e -> {
            event.spawn(StormDisturbance.createRandom(event));
            collector.recordEventTriggered("STORM");
            if (!emergencyRoutesVisible) {
                event.grid.setObjectLocation(emergencyRouteRight, new Int2D(83, 50));
                event.grid.setObjectLocation(emergencyRouteLeft, new Int2D(14, 50));
                event.grid.setObjectLocation(emergencyRouteStraight, new Int2D(50, 15));
                emergencyRoutesVisible = true;
                display.repaint();
            }
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
        legendPanel.add(createSectionTitle("states"), gbc);
        gbc.gridy++;
        legendPanel.add(createCompactLegendEntry("●", Color.GREEN, "Hungry/Thirsty"), gbc);

        gbc.gridy++;
        legendPanel.add(createCompactLegendEntry("●", Color.CYAN, "Watching Sideact"), gbc);
        gbc.gridy++;
        legendPanel.add(createCompactLegendEntry("●", Color.BLUE, "Watching Mainact"), gbc);
        gbc.gridy++;
        legendPanel.add(createCompactLegendEntry("●", Color.RED, "Panic"), gbc);
        gbc.gridy++;
        legendPanel.add(createCompactLegendEntry("●", Color.MAGENTA, "Queue"), gbc);
        gbc.gridy++;
        legendPanel.add(createCompactLegendEntry("●", Color.PINK, "Using WC"), gbc);


        // Spalte 3: Zonen
        gbc.gridx = 2;
        gbc.gridy = 0;
        legendPanel.add(createSectionTitle("Zones"), gbc);
        gbc.gridy++;
        legendPanel.add(createIconLegendEntry(scaledIcon("/images/imbiss-stand.png", 30, 30), "Food"),
                gbc);
        gbc.gridy++;
        legendPanel.add(createIconLegendEntry(scaledIcon("/images/MainAct.png", 30, 30), "Main Stage"),
                gbc);
        gbc.gridy++;
        legendPanel.add(createIconLegendEntry(scaledIcon("/images/SideAct.png", 30, 30), "Side Stage"),
                gbc);
        gbc.gridy++;
        legendPanel.add(createIconLegendEntry(scaledIcon("/images/barrier.png", 30, 30), "Exit"), gbc);
        gbc.gridy++;
        legendPanel.add(
                createIconLegendEntry(scaledIcon("/images/emergency-exit.png", 25, 25), "Emergency"), gbc);
        legendPanel.add(createIconLegendEntry(scaledIcon("/images/emergency-exit.png", 25, 25), "Emergency Exit"), gbc);
        gbc.gridy++;

        legendPanel.add(createIconLegendEntry(scaledIcon("/images/EmergencyRoutRECHTS.png", 25, 25), "Emergency route"), gbc);
        gbc.gridy++;
        legendPanel.add(createIconLegendEntry(scaledIcon("/images/wc2.png", 30, 30), "WC"), gbc);
        gbc.gridy++;
        legendPanel.add(createIconLegendEntry(scaledIcon("/images/fire-station.png", 30, 30), "Fire station"), gbc);
        // Wrapper Panel für Positionierung links unten
        JPanel legendWrapper = new JPanel(new BorderLayout());
        legendWrapper.setOpaque(false);
        legendWrapper.add(legendPanel, BorderLayout.CENTER);

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
            Image img = new ImageIcon(url).getImage()
                    .getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } else {
            System.err.println("❌ Icon nicht gefunden: " + path);
            return new ImageIcon(); // leeres Icon als Fallback
        }
    }

    @Override
    public void quit() {
        super.quit();
        if (frame != null) frame.dispose();
        frame = null;
        display = null;
    }


}
