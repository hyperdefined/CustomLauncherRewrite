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

import javax.swing.*;

public class AccountManagerWindow extends JFrame {

    public AccountManagerWindow(String title) {
        super(title);
        JFrame frame = new JFrame(title);
        frame.setSize(370, 100);
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
        panel.setLayout(null);

        JButton addAccountButton = new JButton("Add Account");
        JButton deleteAccountButton = new JButton("Delete Account");

        addAccountButton.setBounds(50, 15, 130, 30);
        deleteAccountButton.setBounds(200, 15, 130, 30);

        panel.add(addAccountButton);
        panel.add(deleteAccountButton);

        addAccountButton.addActionListener(
                e -> {
                    frame.dispose();
                    JFrame newAccountWindow = new NewAccountWindow("New Account");
                    newAccountWindow.dispose();
                });

        deleteAccountButton.addActionListener(
                e -> {
                    frame.dispose();
                    JFrame deleteAccountWindow = new DeleteAccountWindow("Delete Account");
                    deleteAccountWindow.dispose();
                });

        frame.setVisible(true);
        frame.add(panel);
        frame.setLocationRelativeTo(null);
    }
}
