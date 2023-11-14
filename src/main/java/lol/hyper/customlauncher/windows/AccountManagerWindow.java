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

import lol.hyper.customlauncher.CustomLauncherRewrite;

import javax.swing.*;

public class AccountManagerWindow extends JFrame {

    /**
     * Creates an account manager window.
     *
     * @param mainWindow The MainWindow instance.
     */
    public AccountManagerWindow(MainWindow mainWindow) {
        setTitle("Account Manager");
        setSize(370, 100);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        setIconImage(CustomLauncherRewrite.icon);

        // GUI elements
        JPanel panel = new JPanel();
        panel.setLayout(null);

        JButton addAccountButton = new JButton("Add Account");
        JButton deleteAccountButton = new JButton("Delete Account");

        addAccountButton.setBounds(50, 15, 130, 30);
        deleteAccountButton.setBounds(200, 15, 130, 30);

        panel.add(addAccountButton);
        panel.add(deleteAccountButton);

        addAccountButton.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> {
                NewAccountWindow newAccountWindow = new NewAccountWindow(mainWindow);
                newAccountWindow.setVisible(true);
            });
        });

        deleteAccountButton.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> {
                DeleteAccountWindow deleteAccountWindow = new DeleteAccountWindow(mainWindow);
                deleteAccountWindow.setVisible(true);
            });
        });

        add(panel);
        setLocationRelativeTo(null);
    }
}
