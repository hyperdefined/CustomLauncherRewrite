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

public class ConfigWindow extends JPanel {

    private final Logger logger = LogManager.getLogger(this);

    public ConfigWindow(ConfigHandler configHandler) {
        configHandler.loadConfig(false);

        // GUI elements
        setLayout(null);

        JLabel ttrInstall = new JLabel("<html>TTR Installation</html>");
        JTextField ttrInstallBox = new JTextField(ConfigHandler.INSTALL_LOCATION.getAbsolutePath());
        JLabel showInvasionNotificationsText = new JLabel("<html>Show invasion notifications?</html>");
        JCheckBox showInvasionNotificationsBox = new JCheckBox();
        JLabel showFieldOfficeNotificationsText = new JLabel("<html>Show field office notifications?</html>");
        JCheckBox showFieldOfficeNotificationsBox = new JCheckBox();
        JButton saveButton = new JButton("Save");

        JButton browseButton = new JButton("Browse");
        browseButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            int choice = fileChooser.showOpenDialog(ConfigWindow.this);
            if (choice == JFileChooser.APPROVE_OPTION) {
                String selectedPath = fileChooser.getSelectedFile().getAbsolutePath();
                ttrInstallBox.setText(selectedPath);
            }
        });

        ttrInstallBox.setCaretPosition(0);
        showInvasionNotificationsBox.setSelected(configHandler.showCogInvasionNotifications());
        showFieldOfficeNotificationsBox.setSelected(configHandler.showFieldOfficeNotifications());

        saveButton.addActionListener(
                e -> {
                    String newInstallPath = ttrInstallBox.getText().trim();
                    File testPath = new File(newInstallPath);
                    if (!(testPath.exists()) && !(testPath.isDirectory())) {
                        JOptionPane.showMessageDialog(this, newInstallPath + " is not a valid path!", "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "Settings saved!", "Options", JOptionPane.INFORMATION_MESSAGE);

                        boolean showInvasionNotifications = showInvasionNotificationsBox.isSelected();
                        boolean showFieldOfficeNotifications = showFieldOfficeNotificationsBox.isSelected();

                        configHandler.editConfig("showInvasionNotifications", showInvasionNotifications);
                        configHandler.editConfig("showFieldOfficeNotifications", showFieldOfficeNotifications);
                        configHandler.editConfig("ttrInstallLocation", newInstallPath);

                        logger.info("Saving new config to " + configHandler.CONFIG_FILE.getAbsolutePath());
                        logger.info("ttrInstallLocation: " + newInstallPath);
                        logger.info("showInvasionNotifications: " + showInvasionNotifications);
                        logger.info("showFieldOfficeNotifications: " + showFieldOfficeNotifications);
                    }
                });
        ttrInstall.setBounds(20, 15, 100, 30);
        ttrInstallBox.setBounds(120, 15, 250, 30);
        saveButton.setBounds(20, 200, 60, 30);
        ttrInstallBox.setMaximumSize(new Dimension(200, 25));
        showInvasionNotificationsText.setBounds(20, 60, 100, 80);
        showInvasionNotificationsBox.setBounds(120, 75, 100, 30);
        showFieldOfficeNotificationsText.setBounds(20, 100, 100, 80);
        showFieldOfficeNotificationsBox.setBounds(120, 115, 100, 30);
        browseButton.setBounds(380, 15, 70, 30);

        add(ttrInstall);
        add(ttrInstallBox);
        add(saveButton);
        add(showInvasionNotificationsText);
        add(showInvasionNotificationsBox);
        add(showFieldOfficeNotificationsText);
        add(showFieldOfficeNotificationsBox);
        add(browseButton);
    }
}
