/*
 * This file is part of CustomLauncherRewrite.
 *
 * CustomLauncherRewrite is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CustomLauncherRewrite is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CustomLauncherRewrite.  If not, see <https://www.gnu.org/licenses/>.
 */

package lol.hyper.customlauncher.invasiontracker;

import lol.hyper.customlauncher.generic.ErrorWindow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class InvasionTracker {

    public final DefaultListModel<String> model = new DefaultListModel<>();
    public final HashMap<String, Invasion> invasions = new HashMap<>();
    public final Logger logger = LogManager.getLogger(InvasionTracker.class);
    public ScheduledExecutorService scheduler;

    public void showWindow() {
        JFrame frame = new JFrame("Invasions");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setResizable(false);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // GUI elements
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // invasions label
        JLabel invasionsLabel = new JLabel("Current Invasions");
        invasionsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(invasionsLabel);

        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setHighlighter(null);
        scheduler.scheduleAtFixedRate(() -> textArea.setText(updateInvasionListGUI()), 0, 500, TimeUnit.MILLISECONDS);
        panel.add(textArea);

        frame.pack();
        frame.setSize(500, 400);
        frame.add(panel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /**
     * Updates the invasion list on the actual GUI.
     */
    private String updateInvasionListGUI() {
        List<Invasion> sortedInvasions = new ArrayList<>();
        StringBuilder finalText = new StringBuilder();
        for (Map.Entry<String, Invasion> entry : invasions.entrySet()) {
            sortedInvasions.add(entry.getValue());
        }
        Collections.sort(sortedInvasions);
        for (Invasion invasion : sortedInvasions) {
            String district = invasion.getDistrict();
            String cogType = invasion.getCogType();
            String timeLeft;
            if (invasion.endTime == null) {
                timeLeft = "Estimating...";
            } else {
                timeLeft = convertTime(ChronoUnit.SECONDS.between(LocalDateTime.now(), invasion.endTime));
            }
            finalText
                    .append(district)
                    .append(" - ")
                    .append(cogType)
                    .append(" - ")
                    .append(timeLeft)
                    .append("\n");
        }
        return finalText.toString();
    }

    /**
     * Read invasion API every 5 seconds.
     */
    public void startInvasionRefresh() {
        scheduler = Executors.newScheduledThreadPool(0);
        scheduler.scheduleAtFixedRate(
                () -> {
                    try {
                        readInvasionAPI();
                    } catch (IOException e) {
                        logger.error(e);
                        JFrame errorWindow = new ErrorWindow(
                                "Unable to read invasion API. Please check your log file for more information.");
                        errorWindow.dispose();
                    }
                },
                0,
                10,
                TimeUnit.SECONDS);
    }

    /**
     * Read the TTR API and get the current invasions.
     */
    public void readInvasionAPI() throws IOException {
        String INVASION_URL = "https://www.toontownrewritten.com/api/invasions";
        String invasionJSONRaw = null;

        URL url = new URL(INVASION_URL);
        URLConnection conn = url.openConnection();
        conn.setRequestProperty(
                "User-Agent", "CustomLauncherRewrite https://github.com/hyperdefined/CustomLauncherRewrite");
        conn.connect();
        BufferedReader serverResponse = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        serverResponse.close();

        try (InputStream in = url.openStream()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            invasionJSONRaw = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            reader.close();
        } catch (IOException e) {
            logger.error(e);
        }

        if (invasionJSONRaw == null) {
            logger.warn("invasionJSONRaw returned null. Unable to read the URL?");
            return;
        }

        JSONObject invasionsJSON = new JSONObject(invasionJSONRaw);
        JSONObject invasionsObject = invasionsJSON.getJSONObject("invasions");

        logger.info("Reading " + INVASION_URL + " for current invasions...");
        logger.info(invasionsObject);

        // iterate through each of the invasions (separate JSONs)
        // and add them to the list
        Iterator<String> keys = invasionsObject.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            if (!invasions.containsKey(key)) {
                JSONObject temp = invasionsObject.getJSONObject(key);
                String cogType = temp.getString("type");
                String progress = temp.getString("progress");
                int cogsDefeated = Integer.parseInt(progress.substring(0, progress.indexOf('/')));
                int cogsTotal = Integer.parseInt(progress.substring(progress.indexOf('/') + 1));
                logger.info("New invasion alert! " + key + " Cogs: " + cogsDefeated + "/" + cogsTotal);
                Invasion newInvasion = new Invasion(cogType, cogsDefeated, cogsTotal, key);
                invasions.put(key, newInvasion);
            } else {
                if (!invasions.containsKey(key)) {
                    return; // JUST IN CASE
                }
                Invasion tempInv = invasions.get(key);
                JSONObject temp = invasionsObject.getJSONObject(key);
                String progress = temp.getString("progress");
                int cogsDefeated = Integer.parseInt(progress.substring(0, progress.indexOf('/')));
                int difference = cogsDefeated - tempInv.getCogsDefeated();
                tempInv.updateCogsDefeated(cogsDefeated);
                logger.info(tempInv.getDistrict() + " - " + tempInv.getCogsDefeated() + " cogs");
                logger.info(tempInv.getDistrict() + " - " + difference + " new");
                tempInv.cogsPerMinute = tempInv.cogsPerMinute + difference;
                if (tempInv.counter > 6) {
                    long seconds = ((tempInv.getCogsTotal() - tempInv.getCogsDefeated()) / tempInv.cogsPerMinute) * 60L;
                    logger.info(tempInv.getDistrict() + " - " + seconds + " seconds");
                    logger.info(tempInv.getDistrict() + " - " + tempInv.cogsPerMinute + " per minute");
                    tempInv.endTime = LocalDateTime.now().plusSeconds(seconds);
                    tempInv.counter = 0;
                    tempInv.cogsPerMinute = 0;
                } else {
                    tempInv.counter++;
                }
            }
        }

        Iterator<Map.Entry<String, Invasion>> it = invasions.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Invasion> pair = it.next();
            if (!invasionsObject.has(pair.getKey())) {
                it.remove();
                logger.info("Invasion gone! " + pair.getKey());
            }
        }

        updateInvasionListGUI();
    }

    private String convertTime(long totalSecs)
    {
        long hours = totalSecs / 3600;
        long minutes = (totalSecs % 3600) / 60;
        long seconds = totalSecs % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
