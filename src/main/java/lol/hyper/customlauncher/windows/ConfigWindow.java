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

package lol.hyper.customlauncher.windows;

import lol.hyper.customlauncher.ConfigHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class ConfigWindow extends JFrame {

    private final Logger logger = LogManager.getLogger(this);

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
        JTextField ttrInstallBox = new JTextField(ConfigHandler.INSTALL_LOCATION.getAbsolutePath());
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

                        logger.info(
                                "Saving new config to "
                                        + configHandler.CONFIG_FILE.getAbsolutePath());
                        logger.info("ttrInstallLocation: " + ttrInstallBox.getText());
                        logger.info(
                                "showFieldOfficeNotifications: "
                                        + showFieldOfficeNotificationsBox.isSelected());
                        logger.info(
                                "showInvasionNotifications: "
                                        + showInvasionNotificationsBox.isSelected());
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
