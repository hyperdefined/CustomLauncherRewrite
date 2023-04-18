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

import lol.hyper.customlauncher.Main;
import lol.hyper.customlauncher.accounts.Account;
import lol.hyper.customlauncher.accounts.AccountEncryption;
import lol.hyper.customlauncher.accounts.Accounts;
import lol.hyper.customlauncher.generic.ErrorWindow;
import lol.hyper.customlauncher.login.LoginHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

public class SecretPrompt extends JFrame {

    private final Logger logger = LogManager.getLogger(this);

    public SecretPrompt(Accounts accounts, Account account) {
        JFrame frame = new JFrame("Enter Passphrase");
        frame.setSize(170, 120);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setResizable(false);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        frame.setIconImage(Main.icon);

        // GUI elements
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel secretLabel = new JLabel("Secret Phrase");
        JPasswordField secretText = new JPasswordField();
        JButton loginButton = new JButton("Login");

        panel.add(secretLabel, gbc);
        panel.add(secretText, gbc);
        panel.add(loginButton, gbc);

        // allow pressing enter
        frame.getRootPane().setDefaultButton(loginButton);

        // button listeners
        loginButton.addActionListener(
                event -> {
                    // if the text box is empty
                    if (secretText.getPassword().length != 0) {
                        Account.Type accountType = account.accountType();
                        String realPassword = null;
                        String secret = String.valueOf(secretText.getPassword());
                        switch (accountType) {
                            case PLAINTEXT -> {
                                ErrorWindow errorWindow =
                                        new ErrorWindow(
                                                "Plaintext account was detected, this shouldn't happen.");
                                errorWindow.dispose();
                                return;
                            }
                            case ENCRYPTED -> realPassword =
                                    AccountEncryption.decrypt(account.password(), secret);
                            case LEGACY_ENCRYPTED -> realPassword =
                                    AccountEncryption.decryptLegacy(account.password(), secret);
                        }

                        // realPassword will return null if any exception is thrown
                        // most likely the user entered the wrong passphrase
                        if (realPassword != null) {
                            // if the decryption worked, update the account to new version
                            logger.info(
                                    "Legacy (version 1) account is being used. Converting over to version 2.");
                            account.setAccountType(Account.Type.ENCRYPTED);
                            String newPassword = AccountEncryption.encrypt(realPassword, secret);
                            account.setPassword(newPassword);
                            accounts.writeAccounts();

                            // send the request to login
                            HashMap<String, String> newLoginRequest = new HashMap<>();
                            newLoginRequest.put("username", account.username());
                            newLoginRequest.put("password", realPassword);
                            new LoginHandler(newLoginRequest);
                            frame.dispose();
                            return;
                        }
                        JOptionPane.showMessageDialog(
                                frame,
                                "You entered the wrong passphrase.",
                                "Passphrase Error",
                                JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(
                                frame,
                                "You must enter the passphrase.",
                                "Passphrase Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                });

        frame.add(panel);
        frame.setLocationRelativeTo(null);

        SwingUtilities.invokeLater(() -> frame.setVisible(true));
    }
}
