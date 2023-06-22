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

package lol.hyper.customlauncher.invasions;

import dorkbox.notify.Notify;
import dorkbox.notify.Pos;
import lol.hyper.customlauncher.ConfigHandler;
import lol.hyper.customlauncher.CustomLauncherRewrite;
import lol.hyper.customlauncher.tools.JSONManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class InvasionTracker extends JPanel {

    public final Map<String, Invasion> invasions = new HashMap<>();
    private final JTable invasionTable;
    private final DefaultTableModel invasionTableModel;
    private final JLabel lastFetchedLabel;
    private final SimpleDateFormat lastFetchedFormat = new SimpleDateFormat("hh:mm:ss a");
    public long lastFetched = 0;
    public int runs = 0;
    public boolean isDown = false;
    private final ConfigHandler configHandler;

    private final Logger logger = LogManager.getLogger(this);

    public ScheduledExecutorService executor;

    public JSONObject lastResult;

    /**
     * This tracker will process & display the InvasionTask. It handles the window and tracking
     * of each invasion.
     */
    public InvasionTracker(ConfigHandler configHandler) {
        this.configHandler = configHandler;

        // GUI elements
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        invasionTable = new JTable();
        String[] columns = new String[]{"District", "Cog Type", "Time Left", "Cogs"};

        invasionTableModel = (DefaultTableModel) invasionTable.getModel();
        invasionTableModel.setColumnIdentifiers(columns);
        invasionTable.setDefaultEditor(Object.class, null);
        invasionTable.getTableHeader().setReorderingAllowed(false);
        invasionTable.setFocusable(false);
        JScrollPane scrollPane = new JScrollPane(invasionTable);
        scrollPane.setVisible(true);
        add(scrollPane);

        lastFetchedLabel = new JLabel("Waiting to update...");
        lastFetchedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(lastFetchedLabel);

        // start the refresh for updating the table
        ActionListener actionListener = e -> updateInvasionListGUI();
        Timer timer = new Timer(0, actionListener);
        timer.setDelay(500);
        timer.start();

        // start the timer for reading the API
        startInvasionRefresh();
    }

    /**
     * Updates the invasion list on the actual GUI.
     */
    private void updateInvasionListGUI() {
        invasionTableModel.setRowCount(0);
        // create a separate list of all the invasions
        List<Invasion> sortedInvasions = new ArrayList<>();
        for (Map.Entry<String, Invasion> entry : invasions.entrySet()) {
            sortedInvasions.add(entry.getValue());
        }
        // sort this new list alphabetically
        Collections.sort(sortedInvasions);
        // add the invasions to the table
        for (Invasion invasion : sortedInvasions) {
            String district = invasion.getDistrict();
            String cogType = invasion.getCogType().replace("\u0003", ""); // remove the python char
            String timeLeft;
            long timeLeftSeconds;
            String cogs;
            if (invasion.megaInvasion) {
                cogs = "Mega Invasion";
                timeLeft = "Mega Invasion";
            } else {
                cogs = invasion.getCogsDefeated() + "/" + invasion.getCogsTotal();
                timeLeftSeconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), invasion.endTime);
                if (timeLeftSeconds <= 0) {
                    timeLeft = "Ending soon...";
                } else {
                    timeLeft = convertTime(timeLeftSeconds);
                }
            }
            String[] data = new String[]{district, cogType, timeLeft, cogs};
            invasionTableModel.addRow(data);
        }
        invasionTable.setModel(invasionTableModel);
        Date currentTime = new Date(lastFetched);
        lastFetchedLabel.setText("Last updated: " + lastFetchedFormat.format(currentTime));
    }

    /**
     * Read invasion API every 10 seconds.
     */
    public void startInvasionRefresh() {
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(this::makeRequest, 0, 10, TimeUnit.SECONDS);
    }

    /**
     * Convert seconds to a readable format.
     *
     * @param totalSecs Seconds to convert.
     * @return HH:MM:SS format string.
     */
    private String convertTime(long totalSecs) {
        long hours = totalSecs / 3600;
        long minutes = (totalSecs % 3600) / 60;
        long seconds = totalSecs % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public void showNotification(Invasion invasion, boolean newInvasion) {
        // do not spam the user with all notifications at once
        if (runs == 0) {
            return;
        }

        // do we show notifications?
        if (!configHandler.showCogInvasionNotifications()) {
            return;
        }

        String messageTitle;
        if (newInvasion) {
            messageTitle = "New Invasion!";
        } else {
            messageTitle = "Invasion Gone!";
        }

        Notify notify =
                Notify.create()
                        .title(messageTitle)
                        .text(
                                invasion.getDistrict()
                                        + " - "
                                        + invasion.getCogType().replace("\u0003", ""))
                        .darkStyle()
                        .position(Pos.BOTTOM_RIGHT)
                        .hideAfter(5000)
                        .image(CustomLauncherRewrite.icon);

        notify.show();
    }

    /**
     * Make the request and handle the information it returns.
     */
    private void makeRequest() {
        String INVASION_URL = "https://api.toon.plus/invasions";
        logger.info("Reading " + INVASION_URL + " for current invasions...");
        lastResult = JSONManager.requestJSON(INVASION_URL);

        // if the request failed, stop the task
        if (lastResult == null) {
            isDown = true;
            executor.shutdown();
            return;
        }

       isDown = false; // make sure to set this to false since we can read the API

        // iterate through each of the invasions (separate JSONs)
        Iterator<String> keys = lastResult.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            // each key is stored as district/cogType
            String district = key.substring(0, key.indexOf('/'));
            // if we do not have that invasion stored, create a new invasion object
            // and add it to the list
            if (!invasions.containsKey(district)) {
                JSONObject temp = lastResult.getJSONObject(key);
                String cogType = temp.getString("Type");
                int cogsDefeated = temp.getInt("CurrentProgress");
                int cogsTotal = temp.getInt("MaxProgress");
                boolean megaInvasion = temp.getBoolean("MegaInvasion");
                Invasion newInvasion = new Invasion(district, cogType, cogsTotal, megaInvasion);
                newInvasion.updateCogsDefeated(cogsDefeated);
                newInvasion.endTime =
                        Instant.parse(temp.getString("EstimatedCompletion"))
                                .atZone(ZoneId.systemDefault());
                invasions.put(district, newInvasion);
                showNotification(newInvasion, true);
                logger.info(
                        "Tracking new invasion for "
                                + district
                                + ". Cogs: "
                                + cogsDefeated
                                + "/"
                                + cogsTotal
                                + ". ETA: "
                                + newInvasion.endTime);
            } else {
                if (!invasions.containsKey(district)) {
                    return; // JUST IN CASE
                }
                // if we already have it saved, update the information that we have saved already
                // we want to update the total cogs defeated and the end time
                Invasion tempInv = invasions.get(district);
                JSONObject temp = lastResult.getJSONObject(key);
                // ignore mega invasion cog count
                if (!temp.getBoolean("MegaInvasion")) {
                    int cogsDefeated = temp.getInt("CurrentProgress");
                    logger.info(
                            "Updating invasion details for "
                                    + district
                                    + ". Cogs: "
                                    + tempInv.getCogsDefeated()
                                    + " -> "
                                    + cogsDefeated
                                    + ". ETA: "
                                    + tempInv.endTime);
                    tempInv.updateCogsDefeated(cogsDefeated);
                    tempInv.endTime =
                            Instant.parse(temp.getString("EstimatedCompletion"))
                                    .atZone(ZoneId.systemDefault());
                }
            }
        }

        // we look at the current invasion list and see if any invasions
        // are not on the invasion JSON (aka that invasion is gone)
        Iterator<Map.Entry<String, Invasion>> it = invasions.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Invasion> pair = it.next();
            String cogType = pair.getValue().getCogType();
            String district = pair.getKey();
            // district/cog name
            String key = district + "/" + cogType;
            // if the invasion no longer exists on the API, remove it from our list
            if (!lastResult.has(key)) {
                showNotification(pair.getValue(), false);
                String savedDuration = (System.nanoTime() - pair.getValue().getCacheStartTime()) / 1000000000 + " seconds.";
                it.remove();
                logger.info("Removing saved invasion for " + district + ". Tracked for " + savedDuration);
            }
        }
        runs++;
        lastFetched = System.currentTimeMillis();
    }
}