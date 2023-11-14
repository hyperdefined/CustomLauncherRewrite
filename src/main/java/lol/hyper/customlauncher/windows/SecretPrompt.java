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
import lol.hyper.customlauncher.accounts.AccountEncryption;
import lol.hyper.customlauncher.accounts.Accounts;
import lol.hyper.customlauncher.login.LoginHandler;
import lol.hyper.customlauncher.tools.PopUpWindow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class SecretPrompt extends JFrame {

    /**
     * The SecretPrompt logger.
     */
    private final Logger logger = LogManager.getLogger(this);

    /**
     * Creates a secret prompt window.
     *
     * @param accounts The accounts. This is only used just in case we find legacy accounts we need to update.
     * @param account  The account we are displaying the window for.
     */
    public SecretPrompt(Accounts accounts, Account account) {
        setTitle("Enter Passphrase");
        setSize(170, 120);
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
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel secretLabel = new JLabel("Secret Phrase");
        JPasswordField secretText = new JPasswordField();
        JButton loginButton = new JButton("Login");
        loginButton.requestFocusInWindow();

        panel.add(secretLabel, gbc);
        panel.add(secretText, gbc);
        panel.add(loginButton, gbc);

        // allow pressing enter
        getRootPane().setDefaultButton(loginButton);

        // button listeners
        loginButton.addActionListener(event -> {
            // if the text box is empty
            if (secretText.getPassword().length != 0) {
                Account.Type accountType = account.accountType();
                String realPassword = null;
                String secret = String.valueOf(secretText.getPassword());
                switch (accountType) {
                    case PLAINTEXT -> {
                        new PopUpWindow(this, "Plaintext account was detected, this shouldn't happen.");
                        return;
                    }
                    case ENCRYPTED -> realPassword = AccountEncryption.decrypt(account.password(), secret);
                    case LEGACY_ENCRYPTED -> realPassword = AccountEncryption.decryptLegacy(account.password(), secret);
                }

                // realPassword will return null if any exception is thrown
                // most likely the user entered the wrong passphrase
                if (realPassword != null) {
                    if (account.accountType() == Account.Type.LEGACY_ENCRYPTED) {
                        // if the decryption worked, update the account to new version
                        logger.info("Legacy (version 1) account is being used. Converting over to version 2.");
                        account.setAccountType(Account.Type.ENCRYPTED);
                        String newPassword = AccountEncryption.encrypt(realPassword, secret);
                        account.setPassword(newPassword);
                        accounts.writeAccounts();
                    }

                    // send the request to login
                    Map<String, String> newLoginRequest = new HashMap<>();
                    newLoginRequest.put("username", account.username());
                    newLoginRequest.put("password", realPassword);
                    LoginHandler loginHandler = new LoginHandler(newLoginRequest);
                    loginHandler.login();
                    dispose();
                    return;
                }
                JOptionPane.showMessageDialog(this, "You entered the wrong passphrase.", "Passphrase Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "You must enter the passphrase.", "Passphrase Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        add(panel);
        setLocationRelativeTo(null);
    }
}
