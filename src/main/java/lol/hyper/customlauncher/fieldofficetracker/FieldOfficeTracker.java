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

package lol.hyper.customlauncher.fieldofficetracker;

import dorkbox.notify.Notify;
import dorkbox.notify.Pos;
import lol.hyper.customlauncher.ConfigHandler;
import lol.hyper.customlauncher.CustomLauncherRewrite;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class FieldOfficeTracker extends JFrame {

    public final Map<Integer, FieldOffice> fieldOffices = new HashMap<>();
    private JTable fieldOfficeTable;
    private DefaultTableModel fieldOfficeTableModel;

    private JLabel lastFetchedLabel;
    private final SimpleDateFormat lastFetchedFormat = new SimpleDateFormat("hh:mm:ss a");
    public long lastFetched = 0;
    public int runs = 0;
    public static final Map<Integer, String> zonesToStreets = new HashMap<>();
    public boolean isDown = false;
    public Timer fieldOfficeTaskTimer;
    private final ConfigHandler configHandler;

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
        startFieldOfficeRefresh();
    }

    /**
     * Open the field office window.
     */
    public void showWindow() {
        setTitle("Field Offices");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        setIconImage(CustomLauncherRewrite.icon);

        // GUI elements
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // field office label
        JLabel fieldOfficeLabel = new JLabel("Field Offices");
        fieldOfficeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(fieldOfficeLabel);

        fieldOfficeTable = new JTable();
        String[] columns = new String[]{"Street", "Difficulty", "Total Annexes", "Status"};

        fieldOfficeTableModel = (DefaultTableModel) fieldOfficeTable.getModel();
        fieldOfficeTableModel.setColumnIdentifiers(columns);
        fieldOfficeTable.setDefaultEditor(Object.class, null);
        fieldOfficeTable.getTableHeader().setReorderingAllowed(false);
        fieldOfficeTable.setFocusable(false);
        JScrollPane scrollPane = new JScrollPane(fieldOfficeTable);
        scrollPane.setVisible(true);
        panel.add(scrollPane);

        lastFetchedLabel = new JLabel("Waiting to update...");
        lastFetchedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(lastFetchedLabel);

        ActionListener actionListener = e -> updateFieldOfficeList();
        Timer timer = new Timer(0, actionListener);
        timer.setDelay(500);
        timer.start();

        setSize(500, 400);
        add(panel);
        setLocationRelativeTo(null);

        // stop the schedules here, so they don't run while the window is closed
        addWindowListener(
                new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                        timer.stop();
                    }
                });

        pack();
        setVisible(true);
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
    private void startFieldOfficeRefresh() {
        ActionListener actionListener = new FieldOfficeTask(this);
        fieldOfficeTaskTimer = new Timer(0, actionListener);
        fieldOfficeTaskTimer.setDelay(10000);
        fieldOfficeTaskTimer.start();
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
}
