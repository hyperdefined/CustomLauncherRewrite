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
import lol.hyper.customlauncher.accounts.Account;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DeleteAccountWindow extends JFrame {

    /**
     * The DeleteAccountWindow logger.
     */
    private final Logger logger = LogManager.getLogger(this);

    /**
     * Creates a delete account window.
     *
     * @param mainWindow The MainWindow instance.
     */
    public DeleteAccountWindow(MainWindow mainWindow) {
        setTitle("Delete Account");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        setIconImage(CustomLauncherRewrite.icon);

        // GUI elements
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // accounts label
        JLabel accountsLabel = new JLabel("Accounts (double click to delete)");
        accountsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(accountsLabel);

        DefaultListModel<Account> model = new DefaultListModel<>();
        JList<Account> accountList = new JList<>(model);
        model.addAll(mainWindow.accounts.getAccounts());
        DefaultListCellRenderer renderer = (DefaultListCellRenderer) accountList.getCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.CENTER);
        accountList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        accountList.setLayoutOrientation(JList.VERTICAL);
        JScrollPane scrollBar = new JScrollPane(accountList);
        scrollBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(scrollBar);

        accountList.addMouseListener(
                new MouseAdapter() {
                    public void mouseClicked(MouseEvent evt) {
                        @SuppressWarnings("unchecked")
                        JList<Account> accountList = (JList<Account>) evt.getSource();
                        if (evt.getClickCount() == 2) {
                            Account account = accountList.getSelectedValue();
                            mainWindow.accounts.removeAccount(account);
                            mainWindow.refreshAccountList();
                            JOptionPane.showMessageDialog(
                                    DeleteAccountWindow.this, account.username() + " was deleted!");
                            dispose();
                            logger.info("Deleting account " + account.username());
                        }
                    }
                });

        setSize(300, 400);
        add(panel);
        setLocationRelativeTo(null);
    }
}
