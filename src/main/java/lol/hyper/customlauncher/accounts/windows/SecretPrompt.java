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
import lol.hyper.customlauncher.login.LoginHandler;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

public class SecretPrompt extends JFrame {

    public SecretPrompt(String title, Account account) {
        JFrame frame = new JFrame(title);
        frame.setSize(170, 120);
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
                    if (secretText.getPassword().length != 0) {
                        String username = account.getUsername();
                        String password = account.getPassword();
                        String actualPassword =
                                JSONManager.decrypt(
                                        password, String.valueOf(secretText.getPassword()));

                        if (actualPassword != null) {
                            HashMap<String, String> newLoginRequest = new HashMap<>();
                            newLoginRequest.put("username", username);
                            newLoginRequest.put("password", actualPassword);
                            new LoginHandler(newLoginRequest);
                            frame.dispose();
                        } else {
                            JOptionPane.showMessageDialog(
                                    frame,
                                    "You entered the wrong passphrase.",
                                    "Passphrase Error",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(
                                frame,
                                "You must enter the passphrase.",
                                "Passphrase Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                });

        frame.setVisible(true);
        frame.add(panel);
        frame.setLocationRelativeTo(null);
    }
}
