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

package lol.hyper.customlauncher.accounts.windows;

import lol.hyper.customlauncher.Main;
import lol.hyper.customlauncher.accounts.JSONManager;

import javax.swing.*;
import java.awt.*;

public class OptionsWindow extends JFrame {

    public OptionsWindow(String title) {
        JFrame frame = new JFrame(title);
        frame.setSize(370, 230);
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

        JLabel autoUpdate = new JLabel("<html>Check for TTR updates?</html>");
        JCheckBox autoUpdateBox = new JCheckBox();
        JLabel ttrInstall = new JLabel("<html>TTR Installation</html>");
        JTextField ttrInstallBox = new JTextField(Main.pathToUse);
        JButton saveButton = new JButton("Save");
        if (JSONManager.shouldWeUpdate()) {
            autoUpdateBox.setSelected(true);
        }

        saveButton.addActionListener(e -> {
            boolean checkBox = autoUpdateBox.isSelected();
            String path = ttrInstallBox.getText();
            JSONManager.editConfig("autoCheckTTRUpdates", checkBox, false);
            JSONManager.editConfig("ttrInstallLocation", path, false);
            JOptionPane.showMessageDialog(frame, "Settings saved!", "Options", JOptionPane.INFORMATION_MESSAGE);
        });

        panel.add(autoUpdate);
        panel.add(autoUpdateBox);
        panel.add(ttrInstall);
        panel.add(ttrInstallBox);
        panel.add(saveButton);

        autoUpdate.setBounds(20, 25, 100, 30);
        autoUpdateBox.setBounds(120, 25, 100, 30);
        ttrInstall.setBounds(20, 65, 100, 30);
        ttrInstallBox.setBounds(120, 65, 200, 30);
        saveButton.setBounds(20, 120, 60, 30);
        ttrInstallBox.setMaximumSize(new Dimension(200, 25));

        frame.setVisible(true);
        frame.add(panel);
        frame.setLocationRelativeTo(null);
    }
}