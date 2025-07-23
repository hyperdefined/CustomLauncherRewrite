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
import dorkbox.notify.Position;
import dorkbox.notify.Theme;
import lol.hyper.customlauncher.ConfigHandler;
import lol.hyper.customlauncher.CustomLauncherRewrite;
import lol.hyper.customlauncher.tools.JSONUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.Timer;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class InvasionTrackerPanel extends JPanel {

    /**
     * All current invasions saved locally.
     */
    private final Map<Integer, Invasion> invasions = new HashMap<>();
    /**
     * Cog ID map.
     */
    private final Map<Integer, String> cogMap = new HashMap<>();
    /**
     * District ID map.
     */
    private final Map<Integer, String> districtMap = new HashMap<>();
    /**
     * The table for displaying invasions.
     */
    private final JTable invasionTable;
    /**
     * The model for storing invasions.
     */
    private final DefaultTableModel invasionTableModel;
    /**
     * Label for "last updated."
     */
    private final JLabel lastFetchedLabel;
    /**
     * Date format for "Last updated."
     */
    private final SimpleDateFormat lastFetchedFormat = new SimpleDateFormat("hh:mm:ss a");
    /**
     * When was the last time we made an API request.
     */
    private long lastFetched = 0;
    /**
     * Tracks how many times the tracker runs.
     */
    private int runs = 0;
    /**
     * Tracks if the API is offline.
     */
    private boolean isDown = false;
    /**
     * The ConfigHandler instance.
     */
    private final ConfigHandler configHandler;
    /**
     * The InvasionTrackerPanel logger.
     */
    private final Logger logger = LogManager.getLogger(this);
    /**
     * Scheduler for making API requests.
     */
    private ScheduledExecutorService executor;

    /**
     * Creates a InvasionTrackerPanel. It handles the window and tracking of each invasion.
     */
    public InvasionTrackerPanel(ConfigHandler configHandler) {
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

        // fetch ToonHQ's data
        logger.info("Fetching ToonHQ district data...");
        JSONArray districts = JSONUtils.requestJSONArray("https://toonhq.org/api/districts/");
        logger.info("Fetching ToonHQ cog data...");
        JSONArray cogs = JSONUtils.requestJSONArray("https://toonhq.org/api/cogs/");

        if (districts == null || cogs == null) {
            logger.info("No districts or cogs found");
            logger.info("Cog data: {}", cogs);
            logger.info("District data: {}", districts);
            isDown = true;
            return;
        }

        updateDistrictMap(districts);
        updateCogMap(cogs);

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
        for (Map.Entry<Integer, Invasion> entry : invasions.entrySet()) {
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
            if (invasion.isMegaInvasion()) {
                cogs = "Mega Invasion";
                timeLeft = "Mega Invasion";
            } else {
                cogs = invasion.getCogsDefeated() + "/" + invasion.getCogsTotal();
                timeLeftSeconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), invasion.getEndTime());
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

    /**
     * Show a notification for invasion.
     *
     * @param invasion    The invasion.
     * @param newInvasion Is it a new one?
     */
    public void showNotification(Invasion invasion, boolean newInvasion) {
        // do not spam the user with all notifications at once
        if (runs == 0) {
            return;
        }

        String messageTitle;
        if (newInvasion) {
            messageTitle = "New Invasion!";
        } else {
            messageTitle = "Invasion Gone!";
        }

        String notificationText = invasion.getDistrict() + " - " + invasion.getCogType().replace("\u0003", "");
        Notify notify = Notify.Companion.create().title(messageTitle).text(notificationText).theme(Theme.Companion.getDefaultDark()).position(Position.BOTTOM_RIGHT).hideAfter(5000).image(CustomLauncherRewrite.getIcon());
        notify.show();
    }

    /**
     * Make the request and handle the information it returns.
     */
    private void makeRequest() {
        String INVASION_URL = "https://toonhq.org/api/invasions/1/";
        JSONObject lastResult = JSONUtils.requestJSON(INVASION_URL);

        // if the request failed, stop the task
        if (lastResult == null) {
            isDown = true;
            executor.shutdown();
            return;
        }

        isDown = false; // make sure to set this to false since we can read the API

        // iterate through each of the invasions
        JSONArray invasionsArray = lastResult.getJSONArray("invasions");
        for (int i = 0; i < invasionsArray.length(); i++) {
            JSONObject invasion = invasionsArray.getJSONObject(i);
            int districtId = invasion.getInt("district");
            String districtName = districtMap.get(districtId);
            String cogType = cogMap.get(invasion.getInt("cog"));
            int cogsTotal = invasion.getInt("total");
            int cogsDefeated = invasion.getInt("defeated");
            double defeatRate = invasion.getDouble("defeat_rate");

            // this is a new invasion that we are not tracking currently
            if (!invasions.containsKey(districtId)) {
                // store the information about it
                Invasion newInvasion = new Invasion(districtName, cogType, cogsTotal, false);
                newInvasion.updateCogsDefeated(cogsDefeated);
                newInvasion.setDefeatRate(defeatRate);
                invasions.put(districtId, newInvasion);

                // show notification for it
                if (configHandler.showCogInvasionNotifications()) {
                    showNotification(newInvasion, true);
                }

                // calculate how long it has left (estimate)
                double secondsRemaining = (cogsTotal - cogsDefeated) / defeatRate;
                ZonedDateTime estimatedEnd = Instant.now().plusSeconds((long) secondsRemaining).atZone(ZoneId.systemDefault());
                newInvasion.setEndTime(estimatedEnd);

                logger.info("Tracking new invasion for {}. Cogs: {}/{}", districtName, cogsDefeated, cogsTotal);
            } else {
                // update the information for the invasion
                Invasion tempInv = invasions.get(districtId);
                tempInv.updateCogsDefeated(cogsDefeated);
                tempInv.setDefeatRate(defeatRate);

                // calculate how long it has left (estimate)
                double secondsRemaining = (cogsTotal - cogsDefeated) / defeatRate;
                ZonedDateTime estimatedEnd = Instant.now().plusSeconds((long) secondsRemaining).atZone(ZoneId.systemDefault());
                tempInv.setEndTime(estimatedEnd);

                logger.info("Updating invasion for {}. Cogs: {}/{}", districtName, cogsDefeated, cogsTotal);
            }
        }

        // we look at the current invasion list and see if any invasions
        // are not on the invasion JSON (aka that invasion is gone)
        Iterator<Map.Entry<Integer, Invasion>> it = invasions.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, Invasion> pair = it.next();
            boolean isOnApi = false;
            for (int i = 0; i < invasionsArray.length(); i++) {
                JSONObject invasion = invasionsArray.getJSONObject(i);
                int districtId = invasion.getInt("district");
                if (pair.getKey().equals(districtId)) {
                    isOnApi = true;
                }
            }

            if (!isOnApi) {
                String savedDuration = (System.nanoTime() - pair.getValue().getCacheStartTime()) / 1000000000 + " seconds.";
                if (configHandler.showCogInvasionNotifications()) {
                    showNotification(pair.getValue(), false);
                }
                it.remove();
                logger.info("Removing saved invasion for {}. Tracked for {}", pair.getKey(), savedDuration);
            }
        }
        runs++;
        lastFetched = System.currentTimeMillis();
    }

    /**
     * Update the district ID map from ToonHQ.
     *
     * @param data The JSON data from their API.
     */
    private void updateDistrictMap(JSONArray data) {
        for (int i = 0; i < data.length(); i++) {
            JSONObject district = data.getJSONObject(i);
            int gameId = district.getInt("game");
            // make sure we pull TTR districts (id = 1)
            if (gameId == 1) {
                int id = district.getInt("id");
                String name = district.getString("name");
                districtMap.put(id, name);
            }
        }
    }

    /**
     * Update the cog ID map from ToonHQ.
     *
     * @param data The JSON data from their API.
     */
    private void updateCogMap(JSONArray data) {
        for (int i = 0; i < data.length(); i++) {
            JSONObject cog = data.getJSONObject(i);
            int gameId = cog.getInt("game");
            // make sure we pull TTR cogs (id = 1)
            if (gameId == 1) {
                int id = cog.getInt("id");
                String name = cog.getString("name");
                cogMap.put(id, name);
            }
        }
    }

    /**
     * Is the API down?
     *
     * @return True/false if the API is down.
     */
    public boolean isDown() {
        return isDown;
    }
}