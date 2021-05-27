package lol.hyper.customlauncher.accounts.windows;

import lol.hyper.customlauncher.accounts.Account;
import lol.hyper.customlauncher.accounts.JSONManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;

public class DeleteAccountWindow extends JFrame {

    final HashMap < Integer, String > labelsByIndexes = new HashMap < > ();

    public DeleteAccountWindow(String title) {
        JFrame frame = new JFrame(title);
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

        // accounts label
        JLabel accountsLabel = new JLabel("Accounts (double click to delete)");
        accountsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(accountsLabel);

        // accounts list
        ArrayList < String > accounts = new ArrayList < > ();
        for (int i = 0; i < JSONManager.getAccounts().size(); i++) {
            // get the indexes of the accounts and save them
            Account account = JSONManager.getAccounts().get(i);
            accounts.add(account.getUsername());
            labelsByIndexes.put(i, account.getUsername());
        }
        JList accountList = new JList(accounts.toArray());
        DefaultListCellRenderer renderer = (DefaultListCellRenderer) accountList.getCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.CENTER);
        accountList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        accountList.setLayoutOrientation(JList.VERTICAL);
        JScrollPane scrollBar = new JScrollPane(accountList);
        scrollBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(scrollBar);

        accountList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                JList list = (JList) evt.getSource();
                if (evt.getClickCount() == 2) {
                    int index = list.getSelectedIndex();
                    Account account = JSONManager.getAccounts().get(index);
                    JSONManager.deleteAccount(index);
                    MainWindow.refreshAccountList();
                    JOptionPane.showMessageDialog(frame, account.getUsername() + " was deleted!");
                    frame.dispose();
                }
            }
        });

        frame.pack();
        frame.setSize(300, 400);
        frame.setVisible(true);
        frame.add(panel);
        frame.setLocationRelativeTo(null);
    }
}