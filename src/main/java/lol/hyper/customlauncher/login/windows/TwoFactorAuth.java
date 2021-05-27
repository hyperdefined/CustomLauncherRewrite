package lol.hyper.customlauncher.login.windows;

import lol.hyper.customlauncher.login.LoginHandler;
import lol.hyper.customlauncher.login.LoginRequest;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

public class TwoFactorAuth extends JFrame{

    public TwoFactorAuth(String title, String banner, String token) {
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
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        Box.createVerticalStrut(10);

        JLabel text = new JLabel("<html>" + banner + "</html>");
        text.setAlignmentX(Component.CENTER_ALIGNMENT);
        JTextField userAuthCode = new JTextField();
        userAuthCode.setAlignmentX(Component.CENTER_ALIGNMENT);
        JButton loginButton = new JButton("Submit");
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        userAuthCode.setMaximumSize(new Dimension(50, 25));

        panel.add(text);
        panel.add(userAuthCode);
        panel.add(loginButton);

        // button listeners
        loginButton.addActionListener(event -> {
            LoginRequest newLoginRequest = new LoginRequest();
            newLoginRequest.addDetails("authToken", token);
            newLoginRequest.addDetails("appToken", userAuthCode.getText());
            LoginHandler.handleLoginRequest(newLoginRequest);
            frame.dispose();
        });

        frame.setVisible(true);
        frame.add(panel);
        frame.setLocationRelativeTo(null);
    }
}
