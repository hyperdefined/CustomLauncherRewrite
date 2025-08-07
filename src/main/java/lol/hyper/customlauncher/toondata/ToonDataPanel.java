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

package lol.hyper.customlauncher.toondata;

import lol.hyper.customlauncher.tools.JSONUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ToonDataPanel extends JPanel {

    /**
     * The table for displaying invasions.
     */
    private final JTable dataTable;
    /**
     * The model for storing invasions.
     */
    private final DefaultTableModel dataTableModel;
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
     * Tracks if the API is offline.
     */
    private boolean isDown = false;
    /**
     * Scheduler for making API requests.
     */
    private ScheduledExecutorService executor;
    /**
     * Our session for this time.
     */
    private final String session;
    /**
     * The toon data we pulled from the game.
     */
    private final Map<String, String> toonData = new HashMap<>();
    /**
     * The ToonDataPanel logger.
     */
    private final Logger logger = LogManager.getLogger(this);

    /**
     * Creates a InvasionTrackerPanel. It handles the window and tracking of each invasion.
     */
    public ToonDataPanel() {

        // GUI elements
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        dataTable = new JTable();
        String[] columns = new String[]{"Info", "Value"};

        dataTableModel = (DefaultTableModel) dataTable.getModel();
        dataTableModel.setColumnIdentifiers(columns);
        dataTable.setDefaultEditor(Object.class, null);
        dataTable.getTableHeader().setReorderingAllowed(false);
        dataTable.setFocusable(false);
        JScrollPane scrollPane = new JScrollPane(dataTable);
        scrollPane.setVisible(true);
        add(scrollPane);

        lastFetchedLabel = new JLabel("Waiting to update...");
        lastFetchedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(lastFetchedLabel);

        // start the refresh for updating the table
        ActionListener actionListener = e -> updateDataGUI();
        Timer timer = new Timer(0, actionListener);
        timer.setDelay(500);
        timer.start();

        // use this as a session
        // this can be anything according to the API
        session = String.valueOf(System.currentTimeMillis());
        logger.info("Using session {}", session);

        // start the timer for reading the API
        startDataRefresh();
    }

    /**
     * Updates the invasion list on the actual GUI.
     */
    private void updateDataGUI() {
        dataTableModel.setRowCount(0);

        // put the table data in this order because
        String[] tableKeys = {
                "Toon ID", "Name", "Laff", "Zone", "District", "Neighborhood"
        };
        // add the data to the table
        for (String key : tableKeys) {
            if (toonData.containsKey(key)) {
                String value = toonData.get(key);
                dataTableModel.addRow(new String[]{key, value});
            }
        }
        dataTable.setModel(dataTableModel);

        Date currentTime = new Date(lastFetched);
        lastFetchedLabel.setText("Last updated: " + lastFetchedFormat.format(currentTime));
    }

    /**
     * Read companion API every 10 seconds.
     */
    public void startDataRefresh() {
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(this::makeRequest, 0, 10, TimeUnit.SECONDS);
    }

    /**
     * Make the request and handle the information it returns.
     */
    private void makeRequest() {
        String LOCAL_COMPANION_URL = "http://localhost:1547/all.json";
        JSONObject lastResult = JSONUtils.requestCompanionData(LOCAL_COMPANION_URL, session);

        // if the request failed, stop the task
        if (lastResult == null) {
            isDown = true;
            executor.shutdown();
            return;
        }

        isDown = false; // make sure to set this to false since we can read the API

        JSONObject laff = lastResult.getJSONObject("laff");
        JSONObject toon = lastResult.getJSONObject("toon");
        JSONObject location = lastResult.getJSONObject("location");
        toonData.put("Laff", laff.getInt("current") + "/" + laff.getInt("max"));
        toonData.put("Toon ID", toon.getString("id"));
        toonData.put("Zone", location.getString("zone"));
        toonData.put("District", location.getString("district"));
        toonData.put("Name", toon.getString("name"));
        toonData.put("Neighborhood", location.getString("neighborhood"));

        lastFetched = System.currentTimeMillis();
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
