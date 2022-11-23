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

package lol.hyper.customlauncher.login.windows;

import lol.hyper.customlauncher.Main;
import lol.hyper.customlauncher.login.LoginHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;

public class TwoFactorAuth extends JFrame {

    public TwoFactorAuth(String banner, String token) {
        JFrame frame = new JFrame("Enter Code");
        frame.setSize(230, 150);
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

        JLabel text = new JLabel("<html>" + banner + "</html>");
        JTextField userAuthCode = new JTextField();

        // force the textbox to only have 6 characters
        // 2fa and toonguard codes are 6 long
        userAuthCode.addKeyListener(
                new KeyAdapter() {
                    @Override
                    public void keyTyped(KeyEvent e) {
                        if (userAuthCode.getText().length() >= 6) {
                            e.consume();
                        }
                    }
                });

        JButton loginButton = new JButton("Submit");

        panel.add(text, gbc);
        panel.add(userAuthCode, gbc);
        panel.add(loginButton, gbc);

        // allow pressing enter
        frame.getRootPane().setDefaultButton(loginButton);

        // button listeners
        loginButton.addActionListener(
                event -> {
                    if (!userAuthCode.getText().isEmpty()) {
                        HashMap<String, String> newLoginRequest = new HashMap<>();
                        newLoginRequest.put("authToken", token);
                        newLoginRequest.put("appToken", userAuthCode.getText());
                        new LoginHandler(newLoginRequest);
                        frame.dispose();
                    } else {
                        JOptionPane.showMessageDialog(
                                frame,
                                "You must enter the code.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                });

        frame.setVisible(true);
        frame.add(panel);
        frame.setLocationRelativeTo(null);
    }
}
