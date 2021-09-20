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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class InvasionTracker extends JFrame {

    public final DefaultListModel model = new DefaultListModel();
    public final ArrayList<Invasion> invasions = new ArrayList<>();
    public final Logger logger = LogManager.getLogger(InvasionTracker.class);
    ScheduledExecutorService scheduledExecutorService;

    public InvasionTracker(String title) {
        JFrame frame = new JFrame(title);
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

        scheduledExecutorService = startInvasionRefresh();

        JList invasionList = new JList(model);
        DefaultListCellRenderer renderer = (DefaultListCellRenderer) invasionList.getCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.LEFT);
        invasionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        invasionList.setLayoutOrientation(JList.VERTICAL);
        JScrollPane scrollBar = new JScrollPane(invasionList);
        scrollBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(scrollBar);

        invasionList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                JList list = (JList) evt.getSource();
                if (evt.getClickCount() == 2) {
                    int index = list.getSelectedIndex();
                    Invasion temp = invasions.get(index);
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
        frame.setVisible(true);
        frame.add(panel);
        frame.setLocationRelativeTo(null);

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                scheduledExecutorService.shutdown();
            }
        });
    }

    /**
     * Updates the invasion list on the actual GUI.
     */
    private void updateInvasionListGUI() {
        model.clear();
        for (Invasion invasion : invasions) {
            String temp = invasion.getDistrict() + " - " + invasion.getCogType();
            model.addElement(temp);
        }
    }

    /**
     * Read invasion API every 5 seconds.
     * @return The scheduler.
     */
    public ScheduledExecutorService startInvasionRefresh() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(0);
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
                5,
                TimeUnit.SECONDS);
        return scheduler;
    }

    /**
     * Read the TTR API and get the current invasions.
     */
    public void readInvasionAPI() throws IOException {
        String INVASION_URL = "https://www.toontownrewritten.com/api/invasions";
        String invasionJSONRaw = null;

        invasions.clear();

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


        // iterate through each of the invasions (seperate JSONs)
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
            invasions.add(newInvasion);
        }

        updateInvasionListGUI();
    }
}
