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
import lol.hyper.customlauncher.Main;
import lol.hyper.customlauncher.accounts.Account;
import lol.hyper.customlauncher.accounts.Accounts;
import lol.hyper.customlauncher.accounts.JSONManager;
import lol.hyper.customlauncher.districts.DistrictTracker;
import lol.hyper.customlauncher.fieldofficetracker.FieldOfficeTracker;
import lol.hyper.customlauncher.invasiontracker.InvasionTracker;
import lol.hyper.customlauncher.login.LoginHandler;
import lol.hyper.customlauncher.ttrupdater.TTRUpdater;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;

public final class MainWindow extends JFrame {

    public static final DefaultListModel<Account> accountsModel = new DefaultListModel<>();
    private final InvasionTracker invasionTracker;
    private final FieldOfficeTracker fieldOfficeTracker;
    private final DistrictTracker districtTracker;

    private final Accounts accounts = new Accounts();

    private final Logger logger = LogManager.getLogger(this);

    public MainWindow(ConfigHandler configHandler) {
        JFrame frame = new JFrame("CLR " + Main.version);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
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

        JList<Account> accountList = new JList<>(accountsModel);
        accountsModel.addAll(accounts.getAccounts());
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
                    JFrame accountManagerWindow = new AccountManagerWindow(this);
                    accountManagerWindow.dispose();
                });
        accountManagerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        accountManagerButton.setMaximumSize(
                new Dimension(300, accountManagerButton.getMinimumSize().height));
        panel.add(accountManagerButton);

        // invasions button
        JButton invasionsButton = new JButton("Invasions");
        invasionTracker = new InvasionTracker(configHandler);
        invasionsButton.addActionListener(
                e -> {
                    if (invasionTracker.isDown) {
                        int dialogButton = JOptionPane.YES_NO_OPTION;
                        int dialogResult =
                                JOptionPane.showConfirmDialog(
                                        null,
                                        "It looks like the invasion API is currently offline. Would you like to try checking it again?",
                                        "Error",
                                        dialogButton);
                        if (dialogResult == JOptionPane.YES_OPTION) {
                            invasionTracker.invasionTaskTimer.start();
                        }
                    } else {
                        invasionTracker.showWindow();
                    }
                });
        invasionsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        invasionsButton.setMaximumSize(new Dimension(300, invasionsButton.getMinimumSize().height));
        panel.add(invasionsButton);

        // field office button
        JButton fieldOfficesButton = new JButton("Field Offices");
        fieldOfficeTracker = new FieldOfficeTracker(configHandler);
        fieldOfficesButton.addActionListener(
                e -> {
                    if (fieldOfficeTracker.isDown) {
                        int dialogButton = JOptionPane.YES_NO_OPTION;
                        int dialogResult =
                                JOptionPane.showConfirmDialog(
                                        null,
                                        "It looks like the field office API is currently offline. Would you like to try checking it again?",
                                        "Error",
                                        dialogButton);
                        if (dialogResult == JOptionPane.YES_OPTION) {
                            fieldOfficeTracker.fieldOfficeTaskTimer.start();
                        }
                    } else {
                        fieldOfficeTracker.showWindow();
                    }
                });
        fieldOfficesButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        fieldOfficesButton.setMaximumSize(
                new Dimension(300, fieldOfficesButton.getMinimumSize().height));
        panel.add(fieldOfficesButton);

        // population button
        JButton districtsButton = new JButton("Population");
        districtTracker = new DistrictTracker();
        districtsButton.addActionListener(
                e -> {
                    if (districtTracker.isDown) {
                        int dialogButton = JOptionPane.YES_NO_OPTION;
                        int dialogResult =
                                JOptionPane.showConfirmDialog(
                                        null,
                                        "It looks like the population API is currently offline. Would you like to try checking it again?",
                                        "Error",
                                        dialogButton);
                        if (dialogResult == JOptionPane.YES_OPTION) {
                            districtTracker.districtTaskTimer.start();
                        }
                    } else {
                        districtTracker.showWindow();
                    }
                });
        districtsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        districtsButton.setMaximumSize(new Dimension(300, invasionsButton.getMinimumSize().height));
        panel.add(districtsButton);

        // check for updates button
        JButton ttrUpdateButton = new JButton("Check TTR Updates");
        ttrUpdateButton.addActionListener(
                e -> {
                    // we do this on another thread since it won't properly update the gui
                    Thread t1 = new Thread(() -> new TTRUpdater("Updater"));
                    t1.start();
                });
        ttrUpdateButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        ttrUpdateButton.setMaximumSize(new Dimension(300, ttrUpdateButton.getMinimumSize().height));
        panel.add(ttrUpdateButton);

        // check for updates button
        JButton configButton = new JButton("Configuration");
        configButton.addActionListener(
                e -> {
                    ConfigWindow configWindow = new ConfigWindow(configHandler);
                    configWindow.dispose();
                });
        configButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        configButton.setMaximumSize(new Dimension(300, configButton.getMinimumSize().height));
        panel.add(configButton);

        accountList.addMouseListener(
                new MouseAdapter() {
                    public void mouseClicked(MouseEvent evt) {
                        JList<Account> accountList = (JList<Account>) evt.getSource();
                        if (evt.getClickCount() == 2) {
                            if (!ConfigHandler.INSTALL_LOCATION.exists()) {
                                JOptionPane.showMessageDialog(
                                        frame,
                                        "Unable to launch the game. The install location cannot be found.",
                                        "Error",
                                        JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                            // check if the game is online
                            // before launching
                            boolean isOnline = checkTTRStatus();
                            String status = isOnline ? "online" : "offline";
                            logger.info("TTR is currently: " + status);
                            if (!isOnline) {
                                JOptionPane.showMessageDialog(
                                        frame,
                                        "It looks like TTR is currently offline. Check their website for more info!",
                                        "Error",
                                        JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                            Account selectedAccount = accountList.getSelectedValue();
                            logger.info("Using account: " + selectedAccount.username());
                            if (!selectedAccount.encrypted()) {
                                String username = selectedAccount.username();
                                String password = selectedAccount.password();
                                HashMap<String, String> newLoginRequest = new HashMap<>();
                                newLoginRequest.put("username", username);
                                newLoginRequest.put("password", password);
                                new LoginHandler(newLoginRequest);
                            } else {
                                SecretPrompt secretPrompt = new SecretPrompt(selectedAccount);
                                secretPrompt.dispose();
                            }
                        }
                    }
                });

        frame.setSize(300, 450);
        frame.setVisible(true);
        frame.add(panel);
        frame.setLocationRelativeTo(null);
    }

    /**
     * This will "refresh" the accounts list after adding/deleting an account. This is a dirty
     * trick, but it works fine. This also correctly sorts the accounts by the index from the
     * accounts file.
     */
    public void refreshAccountList() {
        logger.info("Refreshing accounts list window...");
        accountsModel.removeAllElements();
        accountsModel.addAll(accounts.getAccounts());
    }

    private boolean checkTTRStatus() {
        JSONObject ttrStatusJSON =
                JSONManager.requestJSON("https://www.toontownrewritten.com/api/status");
        if (ttrStatusJSON == null) {
            return false;
        }
        return ttrStatusJSON.getBoolean("open");
    }
}
