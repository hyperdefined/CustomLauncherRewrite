package lol.hyper.customlauncher.updater;

import javax.swing.*;

public class InvalidPath extends JFrame {

    public InvalidPath(String title) {
        JFrame frame = new JFrame(title);
        frame.setSize(370, 150);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setResizable(false);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        JOptionPane.showMessageDialog(frame, "Unable to detect TTR install path. Please check config.json and make sure that path is correct.", "Error", JOptionPane.ERROR_MESSAGE);
        frame.setLocationRelativeTo(null);
        frame.dispose();
    }
}