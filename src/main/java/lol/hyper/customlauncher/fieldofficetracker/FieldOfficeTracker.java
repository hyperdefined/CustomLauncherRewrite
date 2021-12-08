package lol.hyper.customlauncher.fieldofficetracker;

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
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class FieldOfficeTracker {

    public final HashMap<Integer, FieldOffice> fieldOffices = new HashMap<>();
    public final Logger logger = LogManager.getLogger(FieldOfficeTracker.class);
    public ScheduledExecutorService schedulerGUI;
    public ScheduledExecutorService schedulerAPI;
    public JTable fieldOfficeTable;
    public DefaultTableModel fieldOfficeTableModel;
    public JFrame frame;
    public static final HashMap<Integer, String> zonesToStreets = new HashMap<>();

    /** Open the field office window. */
    public void showWindow() {
        zonesToStreets.put(3100, "Walrus Way");
        zonesToStreets.put(3200, "Sleet Street");
        zonesToStreets.put(3300, "Polar Place");
        zonesToStreets.put(4100, "Alto Avenue");
        zonesToStreets.put(4200, "Alto Avenue");
        zonesToStreets.put(4300, "Tenor Terrace");
        zonesToStreets.put(5100, "Elm Street");
        zonesToStreets.put(5200, "Maple Street");
        zonesToStreets.put(5300, "Oak Street");
        zonesToStreets.put(9100, "Lullaby Lane");
        zonesToStreets.put(9200, "Pajama Place");

        frame = new JFrame("Field Offices");
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

        // field office label
        JLabel fieldOfficeLabel = new JLabel("Field Offices");
        fieldOfficeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(fieldOfficeLabel);

        fieldOfficeTable = new JTable();
        String[] columns = new String[] {"Street", "Difficulty", "Total Annexes", "Open"};

        fieldOfficeTableModel = (DefaultTableModel) fieldOfficeTable.getModel();
        fieldOfficeTableModel.setColumnIdentifiers(columns);
        fieldOfficeTable.setModel(fieldOfficeTableModel);
        fieldOfficeTable.setDefaultEditor(Object.class, null);
        fieldOfficeTable.getTableHeader().setReorderingAllowed(false);
        fieldOfficeTable.setFocusable(false);
        JScrollPane scrollPane = new JScrollPane(fieldOfficeTable);
        scrollPane.setVisible(true);
        panel.add(scrollPane);

        schedulerGUI = Executors.newScheduledThreadPool(0);
        schedulerGUI.scheduleAtFixedRate(this::updateFieldOfficeList, 0, 1, TimeUnit.SECONDS);

        startFieldOfficeRefresh();

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

    /** Updates the field office list on the actual GUI. */
    private void updateFieldOfficeList() {
        fieldOfficeTableModel.setRowCount(0);
        // create a separate list of all the field offices
        List<FieldOffice> sortedFieldOffice = new ArrayList<>();
        String[] data;
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
            String open = fieldOffice.isOpen();
            data =
                    new String[] {
                        street,
                        String.valueOf(difficulty),
                        String.valueOf(totalAnnexes),
                        String.valueOf(open)
                    };
            fieldOfficeTableModel.addRow(data);
            fieldOfficeTableModel.fireTableDataChanged();
        }
    }

    /** Read field office API every 5 seconds. */
    public void startFieldOfficeRefresh() {
        schedulerAPI = Executors.newScheduledThreadPool(0);
        schedulerAPI.scheduleAtFixedRate(this::readFieldOfficeAPI, 0, 10, TimeUnit.SECONDS);
    }

    /** Read the TTR API and get the current field offices. */
    public void readFieldOfficeAPI() {
        String FIELD_OFFICE_URL = "https://www.toontownrewritten.com/api/fieldoffices";
        String fieldOfficeSJONRaw;

        // make the request to the API
        URL url;
        try {
            url = new URL(FIELD_OFFICE_URL);
        } catch (MalformedURLException e) {
            schedulerAPI.shutdown();
            logger.error("Unable to read field office API!", e);
            JFrame errorWindow =
                    new ErrorWindow(
                            "There was an a problem reading field office API!\n"
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
            logger.error("Unable to read field office API!", e);
            JFrame errorWindow =
                    new ErrorWindow(
                            "There was an a problem reading field office API!\n"
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
            fieldOfficeSJONRaw =
                    reader.lines()
                            .collect(Collectors.joining(System.lineSeparator()))
                            .replace("\u0003", "");
            reader.close();
        } catch (IOException e) {
            schedulerAPI.shutdown();
            logger.error("Unable to read field office API!", e);
            JFrame errorWindow =
                    new ErrorWindow(
                            "There was an a problem reading field office API!\n"
                                    + e.getClass().getCanonicalName()
                                    + ": "
                                    + e.getMessage());
            errorWindow.dispose();
            frame.dispose();
            return;
        }

        // grab the field offices object in the request
        JSONObject fieldOfficeRoot = new JSONObject(fieldOfficeSJONRaw);
        JSONObject fieldOfficeJSON = fieldOfficeRoot.getJSONObject("fieldOffices");

        logger.info("Reading " + FIELD_OFFICE_URL + " for current field offices...");
        logger.info(fieldOfficeJSON);

        Iterator<String> keys = fieldOfficeJSON.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            JSONObject zoneJSON = fieldOfficeJSON.getJSONObject(key);
            logger.info(key + zoneJSON);
            // update field office data
            if (fieldOffices.containsKey(Integer.valueOf(key))) {
                logger.info("Updating field office data for " + key);
                FieldOffice office = fieldOffices.get(Integer.parseInt(key));
                office.setOpen(zoneJSON.getBoolean("open"));
                office.setTotalAnnexes(zoneJSON.getInt("annexes"));
            } else {
                // new field office
                logger.info("New field office at " + key);
                int difficulty = zoneJSON.getInt("difficulty") + 1; // they zero index this
                int totalAnnexes = zoneJSON.getInt("annexes");
                boolean open = zoneJSON.getBoolean("open");
                FieldOffice office =
                        new FieldOffice(Integer.parseInt(key), difficulty, open, totalAnnexes);
                fieldOffices.put(Integer.parseInt(key), office);
            }
        }

        // we look at the current field office list and see if any of them
        // are not on the field office JSON (aka that field office is gone)
        Iterator<Map.Entry<Integer, FieldOffice>> it = fieldOffices.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, FieldOffice> pair = it.next();
            String key = String.valueOf(pair.getKey());
            if (!fieldOfficeJSON.has(key)) {
                it.remove();
                logger.info("Field office gone! " + pair.getValue().getArea());
            }
        }
    }
}
