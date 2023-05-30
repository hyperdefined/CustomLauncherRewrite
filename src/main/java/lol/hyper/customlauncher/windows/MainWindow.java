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
import lol.hyper.customlauncher.CustomLauncherRewrite;
import lol.hyper.customlauncher.accounts.Account;
import lol.hyper.customlauncher.accounts.Accounts;
import lol.hyper.customlauncher.tools.JSONManager;
import lol.hyper.customlauncher.changelog.GameUpdateTracker;
import lol.hyper.customlauncher.changelog.GameUpdatesWindow;
import lol.hyper.customlauncher.districts.DistrictTracker;
import lol.hyper.customlauncher.fieldoffices.FieldOfficeTracker;
import lol.hyper.customlauncher.tools.InfoWindow;
import lol.hyper.customlauncher.invasions.InvasionTracker;
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

    public final Accounts accounts = new Accounts();

    private final Logger logger = LogManager.getLogger(this);

    public MainWindow(ConfigHandler configHandler, GameUpdateTracker gameUpdateTracker) {
        setTitle("CLR " + CustomLauncherRewrite.version);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
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
                e -> SwingUtilities.invokeLater(() -> {
                    AccountManagerWindow accountManagerWindow = new AccountManagerWindow(this);
                    accountManagerWindow.setVisible(true);
                }));
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
                        SwingUtilities.invokeLater(invasionTracker::showWindow);
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
                        SwingUtilities.invokeLater(fieldOfficeTracker::showWindow);
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
                        SwingUtilities.invokeLater(districtTracker::showWindow);
                    }
                });
        districtsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        districtsButton.setMaximumSize(new Dimension(300, invasionsButton.getMinimumSize().height));
        panel.add(districtsButton);

        // check for updates button
        JButton ttrUpdateButton = new JButton("Check TTR Updates");
        ttrUpdateButton.addActionListener(
                e -> SwingUtilities.invokeLater(() -> {
                    TTRUpdater ttrUpdater = new TTRUpdater();
                    ttrUpdater.setVisible(true);

                    SwingWorker<Void, Void> worker = new SwingWorker<>() {
                        @Override
                        protected Void doInBackground() {
                            ttrUpdater.checkUpdates();
                            return null;
                        }
                    };

                    worker.execute();
                }));
        ttrUpdateButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        ttrUpdateButton.setMaximumSize(new Dimension(300, ttrUpdateButton.getMinimumSize().height));
        panel.add(ttrUpdateButton);

        // game updates button
        JButton gameUpdatesButton = new JButton("Game Updates");
        gameUpdatesButton.addActionListener(
                e -> SwingUtilities.invokeLater(() -> {
                    GameUpdatesWindow gameUpdatesWindow = new GameUpdatesWindow(gameUpdateTracker.allGameUpdates);
                    gameUpdatesWindow.setVisible(true);
                }));
        gameUpdatesButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        gameUpdatesButton.setMaximumSize(new Dimension(300, gameUpdatesButton.getMinimumSize().height));
        panel.add(gameUpdatesButton);

        // config button
        JButton configButton = new JButton("Configuration");
        configButton.addActionListener(
                e -> SwingUtilities.invokeLater(() -> {
                    ConfigWindow configWindow = new ConfigWindow(configHandler);
                    configWindow.setVisible(true);
                }));
        configButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        configButton.setMaximumSize(new Dimension(300, configButton.getMinimumSize().height));
        panel.add(configButton);

        accountList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                @SuppressWarnings("unchecked")
                JList<Account> accountList = (JList<Account>) event.getSource();
                if (event.getClickCount() == 2) {
                    // clear the selection
                    accountList.getSelectionModel().clearSelection();
                    if (!ConfigHandler.INSTALL_LOCATION.exists()) {
                        JOptionPane.showMessageDialog(
                                MainWindow.this,
                                "Unable to launch the game. The install location cannot be found.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    // check if the game is online
                    // before launching
                    boolean isOnline = checkTTRStatus();
                    if (!isOnline) {
                        return;
                    }
                    Account selectedAccount = accountList.getSelectedValue();
                    logger.info("Using account: " + selectedAccount.username());
                    Account.Type accountType = selectedAccount.accountType();
                    logger.info("Account type is " + accountType.toInt());
                    switch (accountType) {
                        case ENCRYPTED, LEGACY_ENCRYPTED -> SwingUtilities.invokeLater(() -> {
                            SecretPrompt secretPrompt = new SecretPrompt(accounts, selectedAccount);
                            secretPrompt.setVisible(true);
                        });
                        case PLAINTEXT -> {
                            HashMap<String, String> newLoginRequest = new HashMap<>();
                            newLoginRequest.put("username", selectedAccount.username());
                            newLoginRequest.put("password", selectedAccount.password());
                            new LoginHandler(newLoginRequest);
                        }
                    }
                }
            }
        });

        setSize(300, 450);
        add(panel);
        setLocationRelativeTo(null);
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
                JSONManager.requestJSON("https://toontownrewritten.com/api/status");
        if (ttrStatusJSON == null) {
            return false;
        }

        boolean status = ttrStatusJSON.getBoolean("open");
        logger.info("Game status: " + status);
        // ttr is down, show the banner if there is one
        if (ttrStatusJSON.has("banner")) {
            String banner = ttrStatusJSON.getString("banner");
            logger.info("TTR's banner returned: " + banner);
            InfoWindow infoWindow = new InfoWindow(banner);
            infoWindow.dispose();
        }
        return status;
    }
}
