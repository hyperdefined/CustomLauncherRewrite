package lol.hyper.customlauncher.accounts.windows;

import lol.hyper.customlauncher.accounts.Account;
import lol.hyper.customlauncher.accounts.JSONManager;

import javax.swing.*;
import java.io.IOException;

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
            String username = account.getUsername();
            String password = account.getPassword();
            String actualPassword = JSONManager.decrypt(password, new String(secretText.getPassword()));

            if (actualPassword != null) {
                try {
                    Runtime.getRuntime().exec("C:\\Windows\\System32\\cmd.exe /c start \"\" scripts\\login.bat" + " " + username + " " + actualPassword);
                    frame.dispose();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(frame, "You entered the wrong passprhase.", "Passphrase Error", JOptionPane.ERROR_MESSAGE);
                frame.dispose();
            }
        });

        frame.setVisible(true);
        frame.add(panel);
        frame.setLocationRelativeTo(null);
    }
}
