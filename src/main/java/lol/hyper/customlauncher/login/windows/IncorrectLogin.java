package lol.hyper.customlauncher.login.windows;

import javax.swing.*;

public class IncorrectLogin extends JFrame {

    public IncorrectLogin(String title) {
        JFrame frame = new JFrame(title);
        frame.setSize(370, 150);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setResizable(false);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        JOptionPane.showMessageDialog(frame, "Login details are incorrect.", title, JOptionPane.ERROR_MESSAGE);
        frame.setLocationRelativeTo(null);
        frame.dispose();
    }
}
