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

import lol.hyper.customlauncher.Main;
import lol.hyper.customlauncher.generic.ErrorWindow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
    public ScheduledExecutorService schedulerGUI;
    public ScheduledExecutorService schedulerAPI;
    public JTable invasionTable;
    public DefaultTableModel invasionTableModel;
    public JFrame frame;

    /** Open the invasion window. */
    public void showWindow() {
        frame = new JFrame("Invasions");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
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
        invasionTable.setModel(invasionTableModel);
        invasionTable.setDefaultEditor(Object.class, null);
        invasionTable.getTableHeader().setReorderingAllowed(false);
        invasionTable.setFocusable(false);
        JScrollPane scrollPane = new JScrollPane(invasionTable);
        scrollPane.setVisible(true);
        panel.add(scrollPane);

        schedulerGUI = Executors.newScheduledThreadPool(0);
        schedulerGUI.scheduleAtFixedRate(this::updateInvasionListGUI, 0, 1, TimeUnit.SECONDS);

        startInvasionRefresh();

        frame.pack();
        frame.setSize(500, 400);
        frame.add(panel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        frame.addWindowListener(
                new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                        schedulerAPI.shutdown();
                        schedulerGUI.shutdown();
                    }
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
        // display the invasion in the text box
        // this is just 1 long string that is put into a text field
        for (Invasion invasion : sortedInvasions) {
            String district = invasion.getDistrict();
            String cogType = invasion.getCogType();
            String timeLeft;
            timeLeft =
                    convertTime(ChronoUnit.SECONDS.between(LocalDateTime.now(), invasion.endTime));
            String cogs = invasion.getCogsDefeated() + "/" + invasion.getCogsTotal();
            data = new String[] {district, cogType, timeLeft, cogs};
            invasionTableModel.addRow(data);
            invasionTableModel.fireTableDataChanged();
        }
    }

    /** Read invasion API every 5 seconds. */
    public void startInvasionRefresh() {
        schedulerAPI = Executors.newScheduledThreadPool(0);
        schedulerAPI.scheduleAtFixedRate(this::readInvasionAPI, 0, 10, TimeUnit.SECONDS);
    }

    /** Read the TTR API and get the current invasions. */
    public void readInvasionAPI() {
        String INVASION_URL = "https://api.toon.plus/invasions/";
        String invasionJSONRaw;

        // make the request to the API
        URL url;
        try {
            url = new URL(INVASION_URL);
        } catch (MalformedURLException e) {
            schedulerAPI.shutdown();
            logger.error("Unable to read invasion API!", e);
            JFrame errorWindow =
                    new ErrorWindow(
                            "There was an a problem reading invasion API!\n"
                                    + e.getClass().getCanonicalName()
                                    + ": "
                                    + e.getMessage());
            errorWindow.dispose();
            frame.dispose();
            return;
        }
        URLConnection conn;
        try {
            conn = url.openConnection();
        } catch (IOException e) {
            schedulerAPI.shutdown();
            logger.error("Unable to read invasion API!", e);
            JFrame errorWindow =
                    new ErrorWindow(
                            "There was an a problem reading invasion API!\n"
                                    + e.getClass().getCanonicalName()
                                    + ": "
                                    + e.getMessage());
            errorWindow.dispose();
            frame.dispose();
            return;
        }

        conn.setRequestProperty(
                "User-Agent",
                "CustomLauncherRewrite https://github.com/hyperdefined/CustomLauncherRewrite");

        try (InputStream in = conn.getInputStream()) {
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            invasionJSONRaw = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            reader.close();
        } catch (IOException e) {
            schedulerAPI.shutdown();
            logger.error("Unable to read invasion API!", e);
            JFrame errorWindow =
                    new ErrorWindow(
                            "There was an a problem reading invasion API!\n"
                                    + e.getClass().getCanonicalName()
                                    + ": "
                                    + e.getMessage());
            errorWindow.dispose();
            frame.dispose();
            return;
        }

        // grab the invasions object in the request
        // that hold all the invasions
        JSONObject invasionsJSON = new JSONObject(invasionJSONRaw);

        logger.info("Reading " + INVASION_URL + " for current invasions...");
        logger.info(invasionsJSON);

        // iterate through each of the invasions (separate JSONs)
        Iterator<String> keys = invasionsJSON.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            String district = key.substring(0, key.indexOf('/'));
            // if we do not have that invasion stored, create a new invasion object
            // and add it to the list
            if (!invasions.containsKey(district)) {
                JSONObject temp = invasionsJSON.getJSONObject(key);
                String cogType = temp.getString("Type");
                // remove the unicode string that python puts onto some cogs
                if (cogType.contains("\u0003")) {
                    cogType = cogType.replace("\u0003", "");
                }
                int cogsDefeated = temp.getInt("CurrentProgress");
                int cogsTotal = temp.getInt("MaxProgress");
                logger.info(
                        "New invasion alert! "
                                + district
                                + " Cogs: "
                                + cogsDefeated
                                + "/"
                                + cogsTotal);
                Invasion newInvasion = new Invasion(cogType, cogsDefeated, cogsTotal, district);
                newInvasion.endTime =
                        Instant.parse(temp.getString("EstimatedCompletion"))
                                .atZone(ZoneId.systemDefault());
                invasions.put(district, newInvasion);
            } else {
                if (!invasions.containsKey(district)) {
                    return; // JUST IN CASE
                }
                // if we already have it saved, update the information that we have saved already
                // we want to update the total cogs defeated and the end time
                Invasion tempInv = invasions.get(district);
                JSONObject temp = invasionsJSON.getJSONObject(key);
                int cogsDefeated = temp.getInt("CurrentProgress");
                tempInv.updateCogsDefeated(cogsDefeated);
                tempInv.endTime =
                        Instant.parse(temp.getString("EstimatedCompletion"))
                                .atZone(ZoneId.systemDefault());
            }
        }

        // we look at the current invasion list and see if any invasions
        // are not on the invasion JSON (aka that invasion is gone)
        Iterator<Map.Entry<String, Invasion>> it = invasions.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Invasion> pair = it.next();
            String key = pair.getKey() + "/" + pair.getValue().getCogType();
            if (!invasionsJSON.has(key)) {
                it.remove();
                logger.info("Invasion gone! " + pair.getValue().getDistrict());
            }
        }
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
}
