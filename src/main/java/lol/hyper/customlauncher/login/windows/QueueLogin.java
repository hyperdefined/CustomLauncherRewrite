package lol.hyper.customlauncher.login.windows;

import lol.hyper.customlauncher.login.LoginHandler;
import lol.hyper.customlauncher.login.LoginRequest;

import javax.swing.*;
import java.util.concurrent.TimeUnit;

public class QueueLogin extends JFrame {

    public QueueLogin(String title, String queueToken) {
        JFrame frame = new JFrame(title);
        frame.setSize(370, 150);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setResizable(false);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        JOptionPane.showMessageDialog(frame, "You were placed in a queue. Trying in 5 seconds.", "Queue", JOptionPane.INFORMATION_MESSAGE);
        frame.setLocationRelativeTo(null);
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.addDetails("queueToken", queueToken);
        LoginHandler.handleLoginRequest(loginRequest);
        frame.dispose();
    }
}
