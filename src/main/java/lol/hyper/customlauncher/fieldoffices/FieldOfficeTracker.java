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

package lol.hyper.customlauncher.fieldoffices;

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
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FieldOfficeTracker extends JPanel {

    public final Map<Integer, FieldOffice> fieldOffices = new HashMap<>();
    private final JTable fieldOfficeTable;
    private final DefaultTableModel fieldOfficeTableModel;

    private final JLabel lastFetchedLabel;
    private final SimpleDateFormat lastFetchedFormat = new SimpleDateFormat("hh:mm:ss a");
    public long lastFetched = 0;
    public int runs = 0;
    public static final Map<Integer, String> zonesToStreets = new HashMap<>();
    public boolean isDown = false;
    private final ConfigHandler configHandler;

    private final Logger logger = LogManager.getLogger(this);

    public ScheduledExecutorService executor;

    public JSONObject lastResult;

    /**
     * This tracker will process & display the FieldOfficeTask. It handles the window and tracking
     * of each field office.
     */
    public FieldOfficeTracker(ConfigHandler configHandler) {
        // zone IDs to street names
        zonesToStreets.put(3100, "Walrus Way");
        zonesToStreets.put(3200, "Sleet Street");
        zonesToStreets.put(3300, "Polar Place");
        zonesToStreets.put(4100, "Alto Avenue");
        zonesToStreets.put(4200, "Baritone Boulevard");
        zonesToStreets.put(4300, "Tenor Terrace");
        zonesToStreets.put(5100, "Elm Street");
        zonesToStreets.put(5200, "Maple Street");
        zonesToStreets.put(5300, "Oak Street");
        zonesToStreets.put(9100, "Lullaby Lane");
        zonesToStreets.put(9200, "Pajama Place");
        this.configHandler = configHandler;

        // GUI elements
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // create the table
        fieldOfficeTable = new JTable();
        String[] columns = new String[]{"Street", "Difficulty", "Total Annexes", "Status"};

        // create the table model
        fieldOfficeTableModel = (DefaultTableModel) fieldOfficeTable.getModel();
        fieldOfficeTableModel.setColumnIdentifiers(columns);
        fieldOfficeTable.setDefaultEditor(Object.class, null);
        fieldOfficeTable.getTableHeader().setReorderingAllowed(false);
        fieldOfficeTable.setFocusable(false);
        JScrollPane scrollPane = new JScrollPane(fieldOfficeTable);
        scrollPane.setVisible(true);
        add(scrollPane);

        // store when we last updated
        lastFetchedLabel = new JLabel("Waiting to update...");
        lastFetchedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(lastFetchedLabel);

        // start the refresh for updating the table
        ActionListener actionListener = e -> updateFieldOfficeList();
        Timer timer = new Timer(0, actionListener);
        timer.setDelay(500);
        timer.start();

        // start the timer for reading TTR's API
        startFieldOfficeRefresh();
    }

    /**
     * Updates the field office list on the actual GUI.
     */
    private void updateFieldOfficeList() {
        fieldOfficeTableModel.setRowCount(0);
        // create a separate list of all the field offices
        List<FieldOffice> sortedFieldOffice = new ArrayList<>();
        for (Map.Entry<Integer, FieldOffice> entry : fieldOffices.entrySet()) {
            sortedFieldOffice.add(entry.getValue());
        }
        // sort this new list alphabetically
        Collections.sort(sortedFieldOffice);
        // add the field offices to the table
        for (FieldOffice fieldOffice : sortedFieldOffice) {
            String street = zonesToStreets.get(fieldOffice.getArea());
            int difficulty = fieldOffice.getDifficulty();
            int totalAnnexes = fieldOffice.getTotalAnnexes();
            String open = fieldOffice.status();
            String[] data =
                    new String[]{
                            street,
                            String.valueOf(difficulty),
                            String.valueOf(totalAnnexes),
                            String.valueOf(open)
                    };
            fieldOfficeTableModel.addRow(data);
        }
        fieldOfficeTable.setModel(fieldOfficeTableModel);
        Date currentTime = new Date(lastFetched);
        lastFetchedLabel.setText("Last updated: " + lastFetchedFormat.format(currentTime));
    }

    /**
     * Read field office API every 10 seconds.
     */
    public void startFieldOfficeRefresh() {
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(this::makeRequest, 0, 10, TimeUnit.SECONDS);
    }

    public void showNotification(FieldOffice fieldOffice, boolean newFieldOffice) {
        // do not spam the user with all notifications at once
        if (runs == 0) {
            return;
        }
        // do we show notifications?
        if (!configHandler.showFieldOfficeNotifications()) {
            return;
        }

        String messageTitle;
        if (newFieldOffice) {
            messageTitle = "New Field Office!";
        } else {
            messageTitle = "Field Office Gone!";
        }

        Notify notify =
                Notify.create()
                        .title(messageTitle)
                        .text(
                                zonesToStreets.get(fieldOffice.getArea())
                                        + " - "
                                        + fieldOffice.getDifficulty()
                                        + " star")
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
        String FIELD_OFFICE_URL = "https://www.toontownrewritten.com/api/fieldoffices";
        logger.info("Reading " + FIELD_OFFICE_URL + " for current field offices...");
        lastResult = JSONManager.requestJSON(FIELD_OFFICE_URL);

        // if the request failed, stop the task
        if (lastResult == null) {
            isDown = true;
            executor.shutdown();
            return;
        }

        isDown = false;

        // each field office is under the fieldOffices JSON
        JSONObject fieldOfficeJSON = lastResult.getJSONObject("fieldOffices");

        // go through all the field offices from the API
        Iterator<String> keys = fieldOfficeJSON.keys();
        while (keys.hasNext()) {
            // each field office json is named the zone ID
            // so use this to identify the field office
            int fieldOfficeZone = Integer.parseInt(keys.next());
            JSONObject zoneJSON = fieldOfficeJSON.getJSONObject(String.valueOf(fieldOfficeZone));
            // update field office data if we already have it
            if (fieldOffices.containsKey(fieldOfficeZone)) {
                FieldOffice office = fieldOffices.get(fieldOfficeZone);
                office.setOpen(zoneJSON.getBoolean("open"));
                office.setTotalAnnexes(zoneJSON.getInt("annexes"));
            } else {
                // save the new field office
                int difficulty = zoneJSON.getInt("difficulty") + 1; // they zero index this
                int totalAnnexes = zoneJSON.getInt("annexes");
                boolean open = zoneJSON.getBoolean("open");
                FieldOffice office = new FieldOffice(fieldOfficeZone, difficulty, totalAnnexes);
                office.setOpen(open);
                // add it to our list
                fieldOffices.put(fieldOfficeZone, office);
                showNotification(office, true);
            }
        }

        // we look at the current field office list and see if any of them
        // are not on the field office JSON (aka that field office is gone)
        Iterator<Map.Entry<Integer, FieldOffice>> it = fieldOffices.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, FieldOffice> pair = it.next();
            int key = pair.getKey();
            if (!fieldOfficeJSON.has(String.valueOf(key))) {
                showNotification(pair.getValue(), false);
                it.remove();
            }
        }
        runs++;
        lastFetched = System.currentTimeMillis();
    }
}