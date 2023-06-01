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

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class DistrictTracker extends JPanel {

    public final Map<String, District> districts = new HashMap<>();
    public final JTable districtTable;
    public final JLabel totalPopulationLabel;
    public final DefaultTableModel districtsTableModel;
    public final JLabel lastFetchedLabel;
    public final SimpleDateFormat lastFetchedFormat = new SimpleDateFormat("hh:mm:ss a");
    public long lastFetched = 0;
    public boolean isDown = false;
    public Timer districtTaskTimer;

    /**
     * This tracker will process & display the DistrictTask. It handles the window and tracking of
     * each district.
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
            String[] data =
                    new String[]{
                            name, String.valueOf(population), status
                    };
            districtsTableModel.addRow(data);
            totalPopulation = totalPopulation + district.getPopulation();
        }
        districtTable.setModel(districtsTableModel);
        totalPopulationLabel.setText("Total toons: " + totalPopulation);
        Date currentTime = new Date(lastFetched);
        lastFetchedLabel.setText("Last updated: " + lastFetchedFormat.format(currentTime));
    }

    /**
     * Read population API every 10 seconds.
     */
    public void startDistrictRefresh() {
        ActionListener actionListener = new DistrictTask(this);
        districtTaskTimer = new Timer(0, actionListener);
        districtTaskTimer.setDelay(30000);
        districtTaskTimer.start();
    }
}
