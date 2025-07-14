package metrics;

import javax.swing.*;
import java.util.List;
import java.util.Map;

/**
 * Zeigt eine zusammenfassende Auswertung der gesammelten Metriken nach Beendigung der Simulation.
 * Die Auswertung umfasst Zonen-Besuche, Wartezeiten, Aufenthaltsdauer und ausgelöste Events.
 * Darstellung erfolgt über ein Swing-Dialogfenster.
 *
 * @author cb-235866
 */
public class MetricsViewer {
    private static final double TICKS_PER_MINUTE = 60.0;

    public static void show(MetricsCollector collector) {
        String report = generateReport(collector);
        JOptionPane.showMessageDialog(null, report, "Simulation Scoreboard",
                JOptionPane.PLAIN_MESSAGE);
    }

    //  Extrahiert für Testbarkeit
    public static String generateReport(MetricsCollector collector) {
        StringBuilder sb = new StringBuilder();
        sb.append("🏁 Simulation beendet!\n\n");
        sb.append("📊 Zonen-Betretungen:\n");

        int total = 0;
        for (Map.Entry<String, List<Object>> entry : collector.getAllMetrics().entrySet()) {
            String key = entry.getKey();
            if (key.startsWith("ZoneEntry_")) {
                String zoneName = key.substring("ZoneEntry_".length());
                int count = entry.getValue().size();
                total += count;
                sb.append(String.format("  %-12s → %4d Besuche\n", zoneName, count));
            }
        }
        sb.append(String.format("➤ %-12s → %4d Gesamt\n", "", total));

        sb.append("\n⏱ Wartezeiten in Warteschlangen (Minuten):\n");
        for (Map.Entry<String, List<Object>> entry : collector.getAllMetrics().entrySet()) {
            String key = entry.getKey();
            if (!key.startsWith("QueueWait_")) continue;

            String zoneName = key.substring("QueueWait_".length());
            List<Object> values = entry.getValue();

            if (values.isEmpty()) {
                continue;                        // Zeile komplett überspringen
            }

            long sumTicks = 0;
            long maxTicks = Long.MIN_VALUE;
            for (Object o : values) {
                long t = ((Number) o).longValue();
                sumTicks += t;
                maxTicks = Math.max(maxTicks, t);
            }

            double avgMinutes = (sumTicks / (double) values.size()) / TICKS_PER_MINUTE;
            double maxMinutes = maxTicks / TICKS_PER_MINUTE;

            sb.append(String.format("  %-12s → Ø %6.2f min, max %4.1f min\n", zoneName, avgMinutes,
                    maxMinutes));
        }

        sb.append("\n⏱ Aufenthaltsdauer in Zonen (Minuten):\n");
        for (Map.Entry<String, List<Object>> entry : collector.getAllMetrics().entrySet()) {
            String key = entry.getKey();
            if (!key.startsWith("TimeInZone_")) continue;

            String zoneName = key.substring("TimeInZone_".length());
            List<Object> values = entry.getValue();

            if (values.isEmpty()) {
                continue;                        // Zeile komplett überspringen
            }

            long sumTicks = 0;
            long maxTicks = Long.MIN_VALUE;
            for (Object o : values) {
                long t = ((Number) o).longValue();
                sumTicks += t;
                maxTicks = Math.max(maxTicks, t);
            }

            double avgMinutes = (sumTicks / (double) values.size()) / TICKS_PER_MINUTE;
            double maxMinutes = maxTicks / TICKS_PER_MINUTE;

            sb.append(String.format("  %-12s → Ø %6.2f min, max %4.1f min\n", zoneName, avgMinutes,
                    maxMinutes));
        }

        sb.append("\nAusgelöste Events:\n");
        for (Map.Entry<String, List<Object>> entry : collector.getAllMetrics().entrySet()) {
            String key = entry.getKey();
            if (!key.startsWith("EventTriggered_")) continue;
            String evtName = key.substring("EventTriggered_".length());
            int count = entry.getValue().size();

            String icon;
            switch (evtName) {
                case "FIRE" -> icon = "🔥";
                case "FIGHT" -> icon = "🥊";
                case "STORM" -> icon = "🌩";
                default -> icon = "❔";
            }

            sb.append(String.format("  %s  %-8s → %4d-mal\n", icon, evtName, count));
        }

        return sb.toString();
    }
}
