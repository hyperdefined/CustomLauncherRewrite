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

import lol.hyper.customlauncher.accounts.JSONManager;

import javax.swing.*;

public class NewAccountWindow extends JFrame {
    public NewAccountWindow(String title) {
        super(title);
        JFrame frame = new JFrame(title);
        frame.setSize(370, 600);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setResizable(false);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // GUI elements
        JPanel panel = new JPanel();
        panel.setLayout(null);

        JLabel userLabel = new JLabel("Username");
        JLabel passwordLabel = new JLabel("Password");
        JLabel password2Label = new JLabel("Secret Phrase");
        JTextField userTextField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JPasswordField password2Field = new JPasswordField();
        JButton loginButton = new JButton("Save");
        JButton resetButton = new JButton("Cancel");
        JLabel warning =
                new JLabel(
                        "<html>Secret phrase is used to encrypt and decrypt <br>your password for security when saving your login info.</html>");

        userLabel.setBounds(50, 150, 100, 30);
        passwordLabel.setBounds(50, 190, 100, 30);
        password2Label.setBounds(50, 240, 100, 30);
        userTextField.setBounds(150, 150, 150, 30);
        passwordField.setBounds(150, 190, 150, 30);
        password2Field.setBounds(150, 240, 150, 30);
        loginButton.setBounds(50, 300, 100, 30);
        resetButton.setBounds(200, 300, 100, 30);
        warning.setBounds(50, 350, 370, 100);

        panel.add(userLabel);
        panel.add(passwordLabel);
        panel.add(password2Label);
        panel.add(userTextField);
        panel.add(passwordField);
        panel.add(password2Field);
        panel.add(loginButton);
        panel.add(resetButton);
        panel.add(warning);

        // button listeners
        resetButton.addActionListener(e -> frame.dispose());

        loginButton.addActionListener(
                e -> {
                    boolean userbox = userTextField.getText().isEmpty();
                    boolean passwordBox = passwordField.getPassword().length == 0;
                    boolean password2Box = password2Field.getPassword().length == 0;
                    if (!userbox && !passwordBox && !password2Box) {
                        JSONManager.addNewAccount(
                                userTextField.getText(),
                                passwordField.getPassword(),
                                password2Field.getPassword());
                        MainWindow.refreshAccountList();
                        JOptionPane.showMessageDialog(
                                frame, userTextField.getText() + " was saved!");
                        frame.dispose();
                    } else {
                        JOptionPane.showMessageDialog(
                                frame,
                                "You must fill in all boxes.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                });

        frame.setVisible(true);
        frame.add(panel);
        frame.setLocationRelativeTo(null);
    }
}
