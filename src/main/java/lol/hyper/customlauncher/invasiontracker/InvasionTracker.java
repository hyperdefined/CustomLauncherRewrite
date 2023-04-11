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

import dorkbox.notify.Notify;
import dorkbox.notify.Pos;
import lol.hyper.customlauncher.ConfigHandler;
import lol.hyper.customlauncher.Main;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.*;

public class InvasionTracker {

    public final HashMap<String, Invasion> invasions = new HashMap<>();
    public JTable invasionTable;
    public DefaultTableModel invasionTableModel;
    public JFrame frame;
    public JLabel lastFetchedLabel;
    public final SimpleDateFormat lastFetchedFormat = new SimpleDateFormat("hh:mm:ss a");
    public long lastFetched = 0;
    public int runs = 0;
    public boolean isDown = false;
    public Timer invasionTaskTimer;
    final ConfigHandler configHandler;

    /**
     * This tracker will process & display the InvasionTask. It handles the window and tracking
     * of each invasion.
     */
    public InvasionTracker(ConfigHandler configHandler) {
        this.configHandler = configHandler;
        startInvasionRefresh();
    }

    /** Open the invasion window. */
    public void showWindow() {
        frame = new JFrame("Invasions");
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

        // invasions label
        JLabel invasionsLabel = new JLabel("Current Invasions");
        invasionsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(invasionsLabel);

        invasionTable = new JTable();
        String[] columns = new String[] {"District", "Cog Type", "Time Left", "Cogs"};

        invasionTableModel = (DefaultTableModel) invasionTable.getModel();
        invasionTableModel.setColumnIdentifiers(columns);
        invasionTable.setDefaultEditor(Object.class, null);
        invasionTable.getTableHeader().setReorderingAllowed(false);
        invasionTable.setFocusable(false);
        JScrollPane scrollPane = new JScrollPane(invasionTable);
        scrollPane.setVisible(true);
        panel.add(scrollPane);

        lastFetchedLabel = new JLabel("Waiting to update...");
        lastFetchedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(lastFetchedLabel);

        ActionListener actionListener = e -> updateInvasionListGUI();
        Timer timer = new Timer(0, actionListener);
        timer.setDelay(500);
        timer.start();

        frame.setSize(500, 400);
        frame.add(panel);
        frame.setLocationRelativeTo(null);

        frame.addWindowListener(
                new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                        timer.stop();
                    }
                });

        SwingUtilities.invokeLater(()-> {
            frame.pack();
            frame.setVisible(true);
        });
    }

    /** Updates the invasion list on the actual GUI. */
    private void updateInvasionListGUI() {
        invasionTableModel.setRowCount(0);
        // create a separate list of all the invasions
        List<Invasion> sortedInvasions = new ArrayList<>();
        String[] data;
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
            data = new String[] {district, cogType, timeLeft, cogs};
            invasionTableModel.addRow(data);
            invasionTable.setModel(invasionTableModel);
        }
        Date currentTime = new Date(lastFetched);
        lastFetchedLabel.setText("Last updated: " + lastFetchedFormat.format(currentTime));
    }

    /** Read invasion API every 10 seconds. */
    public void startInvasionRefresh() {
        ActionListener actionListener = new InvasionTask(this);
        invasionTaskTimer = new Timer(0, actionListener);
        invasionTaskTimer.setDelay(10000);
        invasionTaskTimer.start();
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
                        .image(Main.icon);

        notify.show();
    }
}
