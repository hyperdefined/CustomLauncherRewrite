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
import lol.hyper.customlauncher.accounts.Account;
import lol.hyper.customlauncher.accounts.JSONManager;
import lol.hyper.customlauncher.invasiontracker.InvasionTracker;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Paths;
import java.util.HashMap;

public class MainWindow extends JFrame {

    public static final DefaultListModel<String> model = new DefaultListModel<>();
    static final HashMap<Integer, String> labelsByIndexes = new HashMap<>();

    public MainWindow(String title, InvasionTracker invasionTracker) {
        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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

        // accounts label
        JLabel accountsLabel = new JLabel("Accounts (double click)");
        accountsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(accountsLabel);

        // accounts list
        // get the labels from the accounts and show them in the list
        for (int i = 0; i < JSONManager.getAccounts().size(); i++) {
            Account account = JSONManager.getAccounts().get(i);
            labelsByIndexes.put(i, account.getUsername());
            model.addElement(account.getUsername());
        }

        JList<String> accountList = new JList<>(model);
        DefaultListCellRenderer renderer = (DefaultListCellRenderer) accountList.getCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.CENTER);
        accountList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        accountList.setLayoutOrientation(JList.VERTICAL);
        JScrollPane scrollBar = new JScrollPane(accountList);
        scrollBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(scrollBar);

        // new account button
        JButton accountManagerButton = new JButton("Manage Accounts");
        accountManagerButton.addActionListener(
                e -> {
                    JFrame accountManagerWindow = new AccountManagerWindow("Account Manager");
                    accountManagerWindow.dispose();
                });
        accountManagerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        accountManagerButton.setMaximumSize(
                new Dimension(300, accountManagerButton.getMinimumSize().height));
        panel.add(accountManagerButton);

        // invasions button
        JButton invasionsButton = new JButton("Invasions");
        invasionsButton.addActionListener(e -> invasionTracker.showWindow());
        invasionsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        invasionsButton.setMaximumSize(new Dimension(300, invasionsButton.getMinimumSize().height));
        panel.add(invasionsButton);

        accountList.addMouseListener(
                new MouseAdapter() {
                    public void mouseClicked(MouseEvent evt) {
                        JList<String> list = (JList<String>) evt.getSource();
                        if (evt.getClickCount() == 2) {
                            if (!Main.TTR_INSTALL_DIR.exists()) {
                                JOptionPane.showMessageDialog(
                                        frame,
                                        "Unable to launch the game. The install location cannot be found.",
                                        "Error",
                                        JOptionPane.ERROR_MESSAGE);
                            } else {
                                int index = list.getSelectedIndex();
                                Account account = JSONManager.getAccounts().get(index);
                                SecretPrompt secretPrompt =
                                        new SecretPrompt("Enter Passphrase", account);
                                secretPrompt.dispose();
                            }
                        }
                    }
                });

        frame.setSize(300, 400);
        frame.setVisible(true);
        frame.add(panel);
        frame.setLocationRelativeTo(null);
        //frame.pack();
    }

    /**
     * This will "refresh" the accounts list after adding/deleting an account. This is a dirty
     * trick, but it works fine. This also correctly sorts the accounts by the index from the
     * accounts file.
     */
    public static void refreshAccountList() {
        model.removeAllElements();
        for (int i = 0; i < JSONManager.getAccounts().size(); i++) {
            Account account = JSONManager.getAccounts().get(i);
            labelsByIndexes.put(i, account.getUsername());
            model.addElement(account.getUsername());
        }
    }
}
