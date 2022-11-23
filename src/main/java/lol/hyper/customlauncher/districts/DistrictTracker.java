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

import lol.hyper.customlauncher.Main;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class DistrictTracker {

    public final HashMap<String, District> districts = new HashMap<>();
    public JTable districtTable;
    public JLabel totalPopulationLabel;
    public DefaultTableModel districtsTableModel;
    public JFrame frame;
    public JLabel lastFetchedLabel;
    public final SimpleDateFormat lastFetchedFormat = new SimpleDateFormat("hh:mm:ss a");
    public long lastFetched = 0;
    public boolean isDown = false;
    public Timer districtTaskTimer;

    /**
     * This tracker will process & display the DistrictTask. It handles the window and tracking of
     * each district.
     */
    public DistrictTracker() {
        startDistrictRefresh();
    }

    /** Open the population window. */
    public void showWindow() {
        frame = new JFrame("Districts");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setResizable(false);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        frame.setIconImage(Main.icon);

        // GUI elements
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // district label
        JLabel districtsLabel = new JLabel("Districts");
        districtsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(districtsLabel);

        districtTable = new JTable();
        String[] columns = new String[] {"Name", "Population"};

        districtsTableModel = (DefaultTableModel) districtTable.getModel();
        districtsTableModel.setColumnIdentifiers(columns);
        districtTable.setDefaultEditor(Object.class, null);
        districtTable.getTableHeader().setReorderingAllowed(false);
        districtTable.setFocusable(false);
        JScrollPane scrollPane = new JScrollPane(districtTable);
        scrollPane.setVisible(true);
        panel.add(scrollPane);

        // district label
        totalPopulationLabel = new JLabel("Fetching population...");
        totalPopulationLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(totalPopulationLabel);

        lastFetchedLabel = new JLabel("Waiting to update...");
        lastFetchedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(lastFetchedLabel);

        ActionListener actionListener = e -> updateDistricts();
        Timer timer = new Timer(0, actionListener);
        timer.setDelay(500);
        timer.start();

        frame.pack();
        frame.setSize(500, 400);
        frame.add(panel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // stop the schedules here, so they don't run while the window is closed
        frame.addWindowListener(
                new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                        timer.stop();
                    }
                });
    }

    /** Updates the district list on the actual GUI. */
    private void updateDistricts() {
        districtsTableModel.setRowCount(0);
        // create a separate list of all the districts
        List<District> sortedDistricts = new ArrayList<>();
        String[] data;
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
            data =
                    new String[] {
                        name, String.valueOf(population),
                    };
            districtsTableModel.addRow(data);
            districtTable.setModel(districtsTableModel);
            totalPopulation = totalPopulation + district.getPopulation();
        }
        totalPopulationLabel.setText("Total toons: " + totalPopulation);
        Date currentTime = new Date(lastFetched);
        lastFetchedLabel.setText("Last updated: " + lastFetchedFormat.format(currentTime));
    }

    /** Read population API every 10 seconds. */
    private void startDistrictRefresh() {
        ActionListener actionListener = new DistrictTask(this);
        districtTaskTimer = new Timer(0, actionListener);
        districtTaskTimer.setDelay(30000);
        districtTaskTimer.start();
    }
}
