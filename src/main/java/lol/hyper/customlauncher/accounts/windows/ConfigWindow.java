package lol.hyper.customlauncher.accounts.windows;

import lol.hyper.customlauncher.ConfigHandler;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class ConfigWindow extends JFrame {

    public ConfigWindow(ConfigHandler configHandler) {
        configHandler.loadConfig();
        JFrame frame = new JFrame("Configuration");
        frame.setSize(370, 270);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setResizable(false);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // GUI elements
        JPanel panel = new JPanel();
        panel.setLayout(null);

        JLabel ttrInstall = new JLabel("<html>TTR Installation</html>");
        JTextField ttrInstallBox = new JTextField(ConfigHandler.installLocation);
        JLabel showInvasionNotifications = new JLabel("<html>Show invasion notifications?</html>");
        JCheckBox showInvasionNotificationsBox = new JCheckBox();
        JLabel showFieldOfficeNotifications =
                new JLabel("<html>Show field office notifications?</html>");
        JCheckBox showFieldOfficeNotificationsBox = new JCheckBox();
        JButton saveButton = new JButton("Save");

        ttrInstallBox.setCaretPosition(0);
        showInvasionNotificationsBox.setSelected(configHandler.showCogInvasionNotifications());
        showFieldOfficeNotificationsBox.setSelected(configHandler.showFieldOfficeNotifications());

        saveButton.addActionListener(
                e -> {
                    File testPath = new File(ttrInstallBox.getText());
                    if (!(testPath.exists()) && !(testPath.isDirectory())) {
                        JOptionPane.showMessageDialog(
                                frame,
                                ttrInstallBox.getText() + " is not a valid path!",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(
                                frame,
                                "Settings saved!",
                                "Options",
                                JOptionPane.INFORMATION_MESSAGE);
                        this.dispose();
                        configHandler.editConfig(
                                "showInvasionNotifications",
                                showInvasionNotificationsBox.isSelected());
                        configHandler.editConfig(
                                "showFieldOfficeNotifications",
                                showFieldOfficeNotificationsBox.isSelected());
                        configHandler.editConfig("ttrInstallLocation", ttrInstallBox.getText());
                    }
                });

        panel.add(ttrInstall);
        panel.add(ttrInstallBox);
        panel.add(saveButton);
        panel.add(showInvasionNotifications);
        panel.add(showInvasionNotificationsBox);
        panel.add(showFieldOfficeNotifications);
        panel.add(showFieldOfficeNotificationsBox);

        ttrInstall.setBounds(20, 15, 100, 30);
        ttrInstallBox.setBounds(120, 15, 200, 30);
        saveButton.setBounds(20, 170, 60, 30);
        ttrInstallBox.setMaximumSize(new Dimension(200, 25));
        showInvasionNotifications.setBounds(20, 30, 100, 80);
        showInvasionNotificationsBox.setBounds(120, 45, 100, 30);
        showFieldOfficeNotifications.setBounds(20, 70, 100, 80);
        showFieldOfficeNotificationsBox.setBounds(120, 85, 100, 30);

        frame.setVisible(true);
        frame.add(panel);
        frame.setLocationRelativeTo(null);
    }
}
