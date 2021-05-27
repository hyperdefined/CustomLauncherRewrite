package lol.hyper.customlauncher.login.windows;

import lol.hyper.customlauncher.login.LoginHandler;
import lol.hyper.customlauncher.login.LoginRequest;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

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
        panel.setLayout(null);

        JLabel text = new JLabel("<html>" + banner + "</html>");
        text.setAlignmentX(Component.CENTER_ALIGNMENT);
        JTextField userAuthCode = new JTextField();
        userAuthCode.setAlignmentX(Component.CENTER_ALIGNMENT);

        // force the textbox to only have 6 characters
        // 2fa and toonguard codes are 6 long
        userAuthCode.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (userAuthCode.getText().length() >= 6 ) {
                    e.consume();
                }
            }
        });

        JButton loginButton = new JButton("Submit");

        userAuthCode.setMaximumSize(new Dimension(50, 25));
        text.setBounds(20,20,150,30);
        userAuthCode.setBounds(120,65,70,30);

        panel.add(text);
        panel.add(userAuthCode);
        panel.add(loginButton);

        // button listeners
        loginButton.addActionListener(event -> {
            if (!userAuthCode.getText().isEmpty()) {
                LoginRequest newLoginRequest = new LoginRequest();
                newLoginRequest.addDetails("authToken", token);
                newLoginRequest.addDetails("appToken", userAuthCode.getText());
                LoginHandler.handleLoginRequest(newLoginRequest);
                frame.dispose();
            } else {
                JOptionPane.showMessageDialog(frame, "You must enter the code.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        frame.setVisible(true);
        frame.add(panel);
        frame.setLocationRelativeTo(null);
    }
}
