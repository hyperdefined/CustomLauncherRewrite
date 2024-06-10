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
import lol.hyper.customlauncher.releasenotes.ReleaseNotesTracker;
import lol.hyper.customlauncher.releasenotes.ReleaseNotesPanel;
import lol.hyper.customlauncher.districts.DistrictTrackerPanel;
import lol.hyper.customlauncher.fieldoffices.FieldOfficeTrackerPanel;
import lol.hyper.customlauncher.invasions.InvasionTrackerPanel;
import lol.hyper.customlauncher.login.LoginHandler;
import lol.hyper.customlauncher.tools.JSONUtils;
import lol.hyper.customlauncher.tools.PopUpWindow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;

public final class MainWindow extends JFrame {

    /**
     * The list of accounts used to display on the main window.
     */
    private final DefaultListModel<Account> accountsModel = new DefaultListModel<>();
    /**
     * Accounts currently loaded.
     */
    public final Accounts accounts = new Accounts();
    /**
     * The MainWindow logger.
     */
    private final Logger logger = LogManager.getLogger(this);

    /**
     * Creates the main window interface, and mainly handles the entire program.
     *
     * @param gameUpdateTracker The game update tracker that was created before this.
     */
    public MainWindow(ReleaseNotesTracker gameUpdateTracker) {
        setTitle("CLR " + CustomLauncherRewrite.version);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);
        setIconImage(CustomLauncherRewrite.icon);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }

        // Config instance
        ConfigHandler configHandler = new ConfigHandler();

        // tracker stuff
        InvasionTrackerPanel invasionTracker = new InvasionTrackerPanel(configHandler);
        FieldOfficeTrackerPanel fieldOfficeTracker = new FieldOfficeTrackerPanel(configHandler);
        DistrictTrackerPanel districtTracker = new DistrictTrackerPanel();
        ConfigPanel configWindow = new ConfigPanel(configHandler);
        ReleaseNotesPanel gameUpdatesWindow = new ReleaseNotesPanel(gameUpdateTracker);
        AboutPanel aboutPanel = new AboutPanel();

        // GUI elements
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // accounts label
        JLabel accountsLabel = new JLabel("Double click to open account.");
        accountsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(accountsLabel);

        JTabbedPane tabs = new JTabbedPane();

        tabs.addChangeListener(event -> {
            int selectedIndex = tabs.getSelectedIndex();
            switch (selectedIndex) {
                // invasions tab
                case 1 -> {
                    if (invasionTracker.isDown) {
                        int dialogResult = JOptionPane.showConfirmDialog(this, "It looks like the invasion API is down, would you want to try again?", "Invasion Tracker", JOptionPane.YES_NO_OPTION);
                        if (dialogResult == JOptionPane.YES_OPTION) {
                            invasionTracker.startInvasionRefresh();
                        }
                    }
                }

                // field offices tab
                case 2 -> {
                    if (fieldOfficeTracker.isDown) {
                        int dialogResult = JOptionPane.showConfirmDialog(this, "It looks like the field office API is down, would you want to try again?", "Field Office Tracker", JOptionPane.YES_NO_OPTION);
                        if (dialogResult == JOptionPane.YES_OPTION) {
                            fieldOfficeTracker.startFieldOfficeRefresh();
                        }
                    }
                }

                // population tab
                case 3 -> {
                    if (districtTracker.isDown) {
                        int dialogResult = JOptionPane.showConfirmDialog(this, "It looks like the population API is down, would you want to try again?", "Population Tracker", JOptionPane.YES_NO_OPTION);
                        if (dialogResult == JOptionPane.YES_OPTION) {
                            districtTracker.startDistrictRefresh();
                        }
                    }
                }
                // settings tab
                case 5 -> {
                    // update the config to reflect what we have saved
                    configWindow.showInvasionNotificationsBox.setSelected(configHandler.showCogInvasionNotifications());
                    configWindow.showFieldOfficeNotificationsBox.setSelected(configHandler.showFieldOfficeNotifications());
                    configWindow.ttrInstallBox.setText(configHandler.getInstallPath().getAbsolutePath());
                }
            }
        });

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
        accountManagerButton.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            AccountManagerWindow accountManagerWindow = new AccountManagerWindow(this);
            accountManagerWindow.setVisible(true);
        }));
        accountManagerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        accountManagerButton.setMaximumSize(new Dimension(300, accountManagerButton.getMinimumSize().height));
        panel.add(accountManagerButton);

        accountList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                @SuppressWarnings("unchecked") JList<Account> accountList = (JList<Account>) event.getSource();
                if (event.getClickCount() == 2) {
                    // check if the game is online before launching
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
                            LoginHandler loginHandler = new LoginHandler(newLoginRequest);
                            loginHandler.login();
                        }
                    }
                    // clear the selection
                    accountList.getSelectionModel().clearSelection();
                }
            }
        });

        WindowListener listener = new WindowAdapter() {
            public void windowClosing(WindowEvent event) {
                Frame frame = (Frame) event.getSource();
                logger.info("Closing " + frame.getTitle());
            }
        };

        addWindowListener(listener);

        setSize(500, 450);
        tabs.add("Accounts", panel);
        tabs.add("Invasions", invasionTracker);
        tabs.add("Field Offices", fieldOfficeTracker);
        tabs.add("Population", districtTracker);
        tabs.add("Game Updates", gameUpdatesWindow);
        tabs.add("Settings", configWindow);
        tabs.add("About", aboutPanel);
        tabs.setBorder(null);
        add(tabs);
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

    /**
     * Check TTR's status using their API.
     *
     * @return True if open, false if the game is down.
     */
    private boolean checkTTRStatus() {
        JSONObject ttrStatusJSON = JSONUtils.requestJSON("https://toontownrewritten.com/api/status");
        if (ttrStatusJSON == null) {
            return false;
        }

        boolean status = ttrStatusJSON.getBoolean("open");
        logger.info("Game status: " + status);
        // ttr is down, show the banner if there is one
        if (ttrStatusJSON.has("banner")) {
            String banner = ttrStatusJSON.getString("banner");
            logger.info("TTR's banner returned: " + banner);
            new PopUpWindow(this, banner);
        }
        return status;
    }
}
