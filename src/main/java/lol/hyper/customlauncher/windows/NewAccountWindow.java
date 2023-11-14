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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.util.Arrays;

public class NewAccountWindow extends JFrame {

    /**
     * The NewAccountWindow logger.
     */
    private final Logger logger = LogManager.getLogger(this);

    /**
     * Creates a new account window.
     *
     * @param mainWindow The MainWindow instance.
     */
    public NewAccountWindow(MainWindow mainWindow) {
        setTitle("New Account");
        setSize(370, 400);
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

        JLabel usernameLabel = new JLabel("Username");
        JLabel passwordLabel = new JLabel("Password");
        JLabel secretPhraseLabel = new JLabel("Secret Phrase");
        JLabel encryptedLabel = new JLabel("Encrypt Login");
        JTextField usernameTextField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JPasswordField secretPhraseField = new JPasswordField();
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        JCheckBox encryptedCheck = new JCheckBox();
        encryptedCheck.setSelected(false);
        JLabel plaintext = new JLabel("<html>You are currently saving the account in plaintext.<br>If you wish to encrypt this, select the checkbox.</html>");
        JLabel encrypted = new JLabel("<html>You are encrypting the account. The passphrase is used to<br> encrypt it.<br>This passphrase can be anything. You will need to enter it<br>anytime you login.</html>");

        usernameLabel.setBounds(50, 15, 100, 30);
        passwordLabel.setBounds(50, 55, 100, 30);
        secretPhraseLabel.setBounds(50, 135, 100, 30);
        usernameTextField.setBounds(150, 15, 150, 30);
        passwordField.setBounds(150, 55, 150, 30);
        encryptedCheck.setBounds(150, 95, 30, 30);
        encryptedLabel.setBounds(50, 95, 100, 30);
        secretPhraseField.setBounds(150, 135, 150, 30);
        saveButton.setBounds(50, 195, 100, 30);
        cancelButton.setBounds(200, 195, 100, 30);
        plaintext.setBounds(50, 235, 370, 100);
        encrypted.setBounds(50, 235, 370, 100);

        panel.add(usernameLabel);
        panel.add(passwordLabel);
        panel.add(secretPhraseLabel);
        panel.add(usernameTextField);
        panel.add(passwordField);
        panel.add(secretPhraseField);
        panel.add(saveButton);
        panel.add(cancelButton);
        panel.add(plaintext);
        panel.add(encrypted);
        panel.add(encryptedCheck);
        panel.add(encryptedLabel);

        encrypted.setVisible(false);
        secretPhraseField.setVisible(false);
        secretPhraseLabel.setVisible(false);

        // button listeners
        cancelButton.addActionListener(e -> dispose());

        // allow pressing enter
        getRootPane().setDefaultButton(saveButton);

        encryptedCheck.addItemListener(event -> {
            if (event.getStateChange() == ItemEvent.SELECTED) {
                encrypted.setVisible(true);
                plaintext.setVisible(false);
                secretPhraseField.setVisible(true);
                secretPhraseLabel.setVisible(true);
            }
            if (event.getStateChange() == ItemEvent.DESELECTED) {
                encrypted.setVisible(false);
                plaintext.setVisible(true);
                secretPhraseField.setVisible(false);
                secretPhraseLabel.setVisible(false);
            }
        });

        saveButton.addActionListener(e -> {
            boolean usernameIsEmpty = usernameTextField.getText().isEmpty();
            boolean passwordIsEmpty = passwordField.getPassword().length == 0;
            boolean secretIsEmpty = secretPhraseField.getPassword().length == 0;
            boolean encrypt = encryptedCheck.isSelected();
            String enteredUsername = usernameTextField.getText();
            String enteredPassword = Arrays.toString(passwordField.getPassword());
            String enteredPassphrase = Arrays.toString(secretPhraseField.getPassword());
            if (mainWindow.accounts.getUsernames().contains(enteredUsername)) {
                JOptionPane.showMessageDialog(this, "You cannot add an account with the same username.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (usernameIsEmpty || passwordIsEmpty) {
                JOptionPane.showMessageDialog(this, "You must fill in all text boxes.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (encrypt) {
                if (secretIsEmpty) {
                    JOptionPane.showMessageDialog(this, "You must enter a passphrase.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String encryptedPassword = AccountEncryption.encrypt(enteredPassword, enteredPassphrase);
                mainWindow.accounts.addAccount(enteredUsername, encryptedPassword, Account.Type.ENCRYPTED);
            } else {
                mainWindow.accounts.addAccount(enteredUsername, enteredPassword, Account.Type.PLAINTEXT);
            }
            mainWindow.refreshAccountList();
            JOptionPane.showMessageDialog(this, enteredUsername + " was saved!");
            dispose();
            logger.info("Saved new account!");
            logger.info("Username: " + enteredUsername);
            logger.info("Encrypted: " + encrypt);
        });

        add(panel);
        setLocationRelativeTo(null);
    }
}
