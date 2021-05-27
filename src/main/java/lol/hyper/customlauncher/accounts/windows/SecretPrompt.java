package lol.hyper.customlauncher.accounts.windows;

import lol.hyper.customlauncher.accounts.Account;
import lol.hyper.customlauncher.accounts.JSONManager;
import lol.hyper.customlauncher.login.LoginHandler;
import lol.hyper.customlauncher.login.LoginRequest;

import javax.swing.*;

public class SecretPrompt extends JFrame {

    public SecretPrompt(String title, Account account) {
        JFrame frame = new JFrame(title);
        frame.setSize(370, 150);
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

        JLabel secretLabel = new JLabel("Secret Phrase");
        JPasswordField secretText = new JPasswordField();
        JButton loginButton = new JButton("Login");

        secretLabel.setBounds(50,25,100,30);
        secretText.setBounds(150,25,100,30);
        loginButton.setBounds(50,75,100,30);

        panel.add(secretLabel);
        panel.add(secretText);
        panel.add(loginButton);

        // button listeners
        loginButton.addActionListener(event -> {
            if (secretText.getPassword().length != 0) {
                String username = account.getUsername();
                String password = account.getPassword();
                String actualPassword = JSONManager.decrypt(password, new String(secretText.getPassword()));

                if (actualPassword != null) {
                    LoginRequest loginRequest = new LoginRequest();
                    loginRequest.addDetails("username", username);
                    loginRequest.addDetails("password", actualPassword);
                    LoginHandler.handleLoginRequest(loginRequest);
                    frame.dispose();
                } else {
                    JOptionPane.showMessageDialog(frame, "You entered the wrong passphrase.", "Passphrase Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(frame, "You must enter the passphrase.", "Passphrase Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        frame.setVisible(true);
        frame.add(panel);
        frame.setLocationRelativeTo(null);
    }
}
