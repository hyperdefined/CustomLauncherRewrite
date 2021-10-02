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

import lol.hyper.customlauncher.generic.ErrorWindow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class InvasionTracker {

    public final DefaultListModel<Invasion> model = new DefaultListModel<>();
    public final HashMap<String, Invasion> invasions = new HashMap<>();
    public final Logger logger = LogManager.getLogger(InvasionTracker.class);
    public ScheduledExecutorService scheduler;

    public void showWindow() {
        JFrame frame = new JFrame("Invasions");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setResizable(false);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // GUI elements
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // invasions label
        JLabel invasionsLabel = new JLabel("Current Invasions");
        invasionsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(invasionsLabel);

        JList<Invasion> invasionList = new JList<>(model);
        DefaultListCellRenderer renderer = (DefaultListCellRenderer) invasionList.getCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.LEFT);
        invasionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        invasionList.setLayoutOrientation(JList.VERTICAL);
        JScrollPane scrollBar = new JScrollPane(invasionList);
        scrollBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(scrollBar);

        invasionList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                JList<Invasion> list = (JList<Invasion>) evt.getSource();
                if (evt.getClickCount() == 2) {
                    Invasion temp = list.getSelectedValue();
                    String district = temp.getDistrict();
                    int totalCogs = temp.getCogsTotal();
                    int defeatedCogs = temp.getCogsDefeated();
                    JOptionPane.showMessageDialog(
                            frame,
                            district + "\nCogs: " + defeatedCogs + "/" + totalCogs,
                            "Invasion",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        frame.pack();
        frame.setSize(300, 400);
        frame.add(panel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /**
     * Updates the invasion list on the actual GUI.
     */
    private void updateInvasionListGUI() {
        model.clear(); // this sadly makes the list "flash" because it waits for the api to respond
        List<Invasion> sortedInvasions = new ArrayList<>();
        for (Map.Entry<String, Invasion> entry : invasions.entrySet()) {
            sortedInvasions.add(entry.getValue());
        }
        Collections.sort(sortedInvasions);
        for (Invasion invasion : sortedInvasions) {
            model.addElement(invasion);
        }
    }

    /**
     * Read invasion API every 5 seconds.
     */
    public void startInvasionRefresh() {
        scheduler = Executors.newScheduledThreadPool(0);
        scheduler.scheduleAtFixedRate(
                () -> {
                    try {
                        readInvasionAPI();
                    } catch (IOException e) {
                        logger.error(e);
                        JFrame errorWindow = new ErrorWindow(
                                "Unable to read invasion API. Please check your log file for more information.");
                        errorWindow.dispose();
                    }
                },
                0,
                10,
                TimeUnit.SECONDS);
    }

    /**
     * Read the TTR API and get the current invasions.
     */
    public void readInvasionAPI() throws IOException {
        String INVASION_URL = "https://www.toontownrewritten.com/api/invasions";
        String invasionJSONRaw = null;

        URL url = new URL(INVASION_URL);
        URLConnection conn = url.openConnection();
        conn.setRequestProperty(
                "User-Agent", "CustomLauncherRewrite https://github.com/hyperdefined/CustomLauncherRewrite");
        conn.connect();
        BufferedReader serverResponse = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        serverResponse.close();

        try (InputStream in = url.openStream()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            invasionJSONRaw = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            reader.close();
        } catch (IOException e) {
            logger.error(e);
        }

        if (invasionJSONRaw == null) {
            logger.warn("invasionJSONRaw returned null. Unable to read the URL?");
            return;
        }

        JSONObject invasionsJSON = new JSONObject(invasionJSONRaw);
        JSONObject invasionsObject = invasionsJSON.getJSONObject("invasions");

        logger.info("Reading " + INVASION_URL + " for current invasions...");
        logger.info(invasionsObject);

        HashMap<String, Invasion> newInvasions = new HashMap<>();

        // iterate through each of the invasions (separate JSONs)
        // and add them to the list
        Iterator<String> keys = invasionsObject.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            JSONObject temp = invasionsObject.getJSONObject(key);
            String cogType = temp.getString("type");
            String progress = temp.getString("progress");
            int cogsDefeated = Integer.parseInt(progress.substring(0, progress.indexOf('/')));
            int cogsTotal = Integer.parseInt(progress.substring(progress.indexOf('/') + 1));
            long time = temp.getLong("asOf");
            Invasion newInvasion = new Invasion(cogType, cogsDefeated, cogsTotal, key, time);
            newInvasions.put(key, newInvasion);
        }

        // these 2 for loops will check for new and old invasions
        // there is probably a much better way to handle this
        // alerts will pop up here
        // TODO: add some type of alert here
        for (Map.Entry<String, Invasion> entry : newInvasions.entrySet()) {
            // this is a NEW invasion
            if (!invasions.containsKey(entry.getKey())) {
                invasions.put(entry.getKey(), entry.getValue());
                logger.info("New invasion alert! " + entry.getKey());
            }
        }

        for (String districtName : invasions.keySet()) {
            // this is an OLD invasion that is gone
            if (!newInvasions.containsKey(districtName)) {
                logger.info("Invasion is gone! " + districtName);
                invasions.remove(districtName);
            }
        }

        updateInvasionListGUI();
    }
}
