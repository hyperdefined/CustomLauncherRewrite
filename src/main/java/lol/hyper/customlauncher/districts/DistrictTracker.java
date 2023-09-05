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

package lol.hyper.customlauncher.districts;

import lol.hyper.customlauncher.tools.JSONUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DistrictTracker extends JPanel {

    /**
     * All current districts saved locally.
     */
    private final Map<String, District> districts = new HashMap<>();
    /**
     * The table for displaying districts.
     */
    private final JTable districtTable;
    /**
     * Label for "total toons."
     */
    private final JLabel totalPopulationLabel;
    /**
     * The model for storing districts.
     */
    private final DefaultTableModel districtsTableModel;
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
    public boolean isDown = false;
    /**
     * The DistrictTracker logger.
     */
    private final Logger logger = LogManager.getLogger(this);
    /**
     * Scheduler for making API requests.
     */
    private ScheduledExecutorService executor;


    /**
     * Handle reading and displaying information from the population API.
     */
    public DistrictTracker() {
        // GUI elements
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // create the table
        districtTable = new JTable();
        String[] columns = new String[]{"Name", "Population", "Status"};

        // create the table model
        districtsTableModel = (DefaultTableModel) districtTable.getModel();
        districtsTableModel.setColumnIdentifiers(columns);
        districtTable.setDefaultEditor(Object.class, null);
        districtTable.getTableHeader().setReorderingAllowed(false);
        districtTable.setFocusable(false);
        JScrollPane scrollPane = new JScrollPane(districtTable);
        add(scrollPane);

        // district label
        totalPopulationLabel = new JLabel("Fetching population...");
        totalPopulationLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(totalPopulationLabel);

        // store when we last updated
        lastFetchedLabel = new JLabel("Waiting to update...");
        lastFetchedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(lastFetchedLabel);

        // start the refresh for updating the table
        ActionListener actionListener = e -> updateDistricts();
        Timer timer = new Timer(0, actionListener);
        timer.setDelay(500);
        timer.start();

        startDistrictRefresh();
    }

    /**
     * Updates the district list on the actual GUI.
     */
    private void updateDistricts() {
        districtsTableModel.setRowCount(0);
        // create a separate list of all the districts
        List<District> sortedDistricts = new ArrayList<>();
        for (Map.Entry<String, District> entry : districts.entrySet()) {
            sortedDistricts.add(entry.getValue());
        }
        // sort this new list alphabetically
        Collections.sort(sortedDistricts);

        int totalPopulation = 0;

        // add the district to the table
        for (District district : sortedDistricts) {
            String name = district.getDistrictName();
            int population = district.getPopulation();
            String status = district.getCurrentStatus();
            String[] data = new String[]{name, String.valueOf(population), status};
            districtsTableModel.addRow(data);
            totalPopulation = totalPopulation + district.getPopulation();
        }
        districtTable.setModel(districtsTableModel);
        totalPopulationLabel.setText("Total toons: " + totalPopulation);
        Date currentTime = new Date(lastFetched);
        lastFetchedLabel.setText("Last updated: " + lastFetchedFormat.format(currentTime));
    }

    /**
     * Read population API every 30 seconds.
     */
    public void startDistrictRefresh() {
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(this::makeRequest, 0, 30, TimeUnit.SECONDS);
    }

    /**
     * Make the request and handle the information it returns.
     */
    private void makeRequest() {
        String DISTRICT_URL = "https://www.toontownrewritten.com/api/population";
        logger.info("Reading " + DISTRICT_URL + " for current population...");
        JSONObject lastResult = JSONUtils.requestJSON(DISTRICT_URL);

        // if the request failed, stop the task
        if (lastResult == null) {
            isDown = true;
            executor.shutdown();
            return;
        }

        isDown = false; // make sure to set this to false since we can read the API

        logger.info("Reading " + DISTRICT_URL + " for current districts...");

        JSONObject populationByDistrict = lastResult.getJSONObject("populationByDistrict");
        // iterate through each district
        Iterator<String> populationKeys = populationByDistrict.keys();
        while (populationKeys.hasNext()) {
            String districtFromJSON = populationKeys.next();
            // if we do not have that district stored, create a new district object
            // and add it to the list
            if (!districts.containsKey(districtFromJSON)) {
                int population = populationByDistrict.getInt(districtFromJSON);
                District district = new District(districtFromJSON);
                district.setPopulation(population);
                districts.put(districtFromJSON, district);
            } else {
                if (!districts.containsKey(districtFromJSON)) {
                    return; // JUST IN CASE
                }
                // if we already have it saved, update the population
                District tempDistrict = districts.get(districtFromJSON);
                int population = populationByDistrict.getInt(districtFromJSON);
                tempDistrict.setPopulation(population);
            }
        }

        JSONObject statusByDistrict = lastResult.getJSONObject("statusByDistrict");
        // iterate through each district
        Iterator<String> statusKeys = statusByDistrict.keys();
        while (statusKeys.hasNext()) {
            String districtFromJSON = statusKeys.next();
            // only update the status of districts we track
            if (districts.containsKey(districtFromJSON)) {
                String status = statusByDistrict.getString(districtFromJSON);
                District tempDistrict = districts.get(districtFromJSON);
                tempDistrict.setCurrentStatus(status);
            }
        }
        lastFetched = System.currentTimeMillis();
    }
}