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

    public final HashMap<String, Invasion> invasions = new HashMap<>();
    public final Logger logger = LogManager.getLogger(InvasionTracker.class);
    public ScheduledExecutorService scheduler;
    public boolean showDurations;

    /**
     * Open the invasion window.
     */
    public void showWindow(boolean showDurations) {
        JFrame frame = new JFrame("Invasions");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setResizable(false);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        this.showDurations = showDurations;

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
        // create a separate list of all the invasions
        List<Invasion> sortedInvasions = new ArrayList<>();
        StringBuilder finalText = new StringBuilder();
        for (Map.Entry<String, Invasion> entry : invasions.entrySet()) {
            sortedInvasions.add(entry.getValue());
        }
        // sort this new list alphabetically
        Collections.sort(sortedInvasions);
        // display the invasion in the text box
        // this is just 1 long string that is put into a text field
        for (Invasion invasion : sortedInvasions) {
            String district = invasion.getDistrict();
            String cogType = invasion.getCogType();
            if (showDurations) {
                String timeLeft;
                // if there is no end time calculated
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
            } else {
                finalText
                        .append(district)
                        .append(" - ")
                        .append(cogType)
                        .append(" - ")
                        .append("(")
                        .append(invasion.getCogsDefeated())
                        .append("/")
                        .append(invasion.getCogsTotal())
                        .append(")")
                        .append("\n");
            }
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
                        // clear the invasions JUST to be safe
                        invasions.clear();
                        // restart the scheduler
                        scheduler.shutdown();
                        startInvasionRefresh();
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

        // make the request to the API
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

        // grab the invasions object in the request
        // that hold all the invasions
        JSONObject invasionsJSON = new JSONObject(invasionJSONRaw);
        JSONObject invasionsObject = invasionsJSON.getJSONObject("invasions");

        logger.info("Reading " + INVASION_URL + " for current invasions...");
        logger.info(invasionsObject);

        // iterate through each of the invasions (separate JSONs)
        Iterator<String> keys = invasionsObject.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            // if we do not have that invasion stored, create a new invasion object
            // and add it to the list
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
                // if we already have it saved, update the information that we have saved already
                // we want to update the total cogs defeated, so we can calculate the end time
                Invasion tempInv = invasions.get(key);
                JSONObject temp = invasionsObject.getJSONObject(key);
                String progress = temp.getString("progress");
                int cogsDefeated = Integer.parseInt(progress.substring(0, progress.indexOf('/')));
                // if we want to show invasions, then calculate the cogs per min
                if (showDurations) {
                    int difference = cogsDefeated - tempInv.getCogsDefeated();
                    tempInv.updateCogsDefeated(cogsDefeated);
                    logger.info(tempInv.getDistrict() + " - " + tempInv.getCogsDefeated() + " cogs");
                    logger.info(tempInv.getDistrict() + " - " + difference + " new");
                    tempInv.cogsPerMinute = tempInv.cogsPerMinute + difference;
                    // each invasion has this counter to track how many times it was already looked at
                    // we then take the total cogs defeated in 1 minute and use that to calculate
                    // the invasion end time
                    if (tempInv.counter > 6) {
                        long seconds =
                                ((tempInv.getCogsTotal() - tempInv.getCogsDefeated()) / tempInv.cogsPerMinute) * 60L;
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
        }

        // we look at the current invasion list and see if any invasions
        // are not on the invasion JSON (aka that invasion is gone)
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

    private String convertTime(long totalSecs) {
        long hours = totalSecs / 3600;
        long minutes = (totalSecs % 3600) / 60;
        long seconds = totalSecs % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
