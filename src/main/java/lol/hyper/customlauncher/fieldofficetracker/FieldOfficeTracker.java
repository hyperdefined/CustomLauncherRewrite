package lol.hyper.customlauncher.fieldofficetracker;

import dorkbox.notify.Notify;
import dorkbox.notify.Pos;
import lol.hyper.customlauncher.Main;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

public class FieldOfficeTracker {

    public final HashMap<Integer, FieldOffice> fieldOffices = new HashMap<>();
    public final Logger logger = LogManager.getLogger(FieldOfficeTracker.class);
    public JTable fieldOfficeTable;
    public DefaultTableModel fieldOfficeTableModel;
    public JFrame frame;
    public static final HashMap<Integer, String> zonesToStreets = new HashMap<>();
    int calls = 0;

    public FieldOfficeTracker() {
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
        startFieldOfficeRefresh();
    }

    /** Open the field office window. */
    public void showWindow() {
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
        String[] columns = new String[] {"Street", "Difficulty", "Total Annexes", "Status"};

        fieldOfficeTableModel = (DefaultTableModel) fieldOfficeTable.getModel();
        fieldOfficeTableModel.setColumnIdentifiers(columns);
        fieldOfficeTable.setDefaultEditor(Object.class, null);
        fieldOfficeTable.getTableHeader().setReorderingAllowed(false);
        fieldOfficeTable.setFocusable(false);
        JScrollPane scrollPane = new JScrollPane(fieldOfficeTable);
        scrollPane.setVisible(true);
        panel.add(scrollPane);

        ActionListener actionListener = e -> updateFieldOfficeList();
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
            String open = fieldOffice.status();
            data =
                    new String[] {
                        street,
                        String.valueOf(difficulty),
                        String.valueOf(totalAnnexes),
                        String.valueOf(open)
                    };
            fieldOfficeTableModel.addRow(data);
            fieldOfficeTable.setModel(fieldOfficeTableModel);
        }
    }

    /** Read field office API every 5 seconds. */
    private void startFieldOfficeRefresh() {
        ActionListener actionListener = new FieldOfficeTask(this);
        Timer timer = new Timer(0, actionListener);
        timer.setDelay(5000);
        timer.start();
    }

    public void showNotification(FieldOffice fieldOffice, boolean newFieldOffice) {
        // don't spam the user with a bunch of notifications at once when we first launch
        if (calls == 0) {
            return;
        }
        Notify notify;
        if (newFieldOffice) {
            notify = Notify.create().title("New Field Office!").text(zonesToStreets.get(fieldOffice.getArea()) + " - " + fieldOffice.getDifficulty() + " star").darkStyle().position(Pos.BOTTOM_RIGHT).hideAfter(5000).image(Main.icon);
        } else {
            notify = Notify.create().title("Field Office Gone!").text(zonesToStreets.get(fieldOffice.getArea()) + " - " + fieldOffice.getDifficulty() + " star").darkStyle().position(Pos.BOTTOM_RIGHT).hideAfter(5000).image(Main.icon);
        }
        notify.showInformation();
    }
}
