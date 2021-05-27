package lol.hyper.customlauncher.accounts.windows;

import lol.hyper.customlauncher.accounts.Account;
import lol.hyper.customlauncher.accounts.JSONManager;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;

public class MainWindow extends JFrame {

    static final HashMap<Integer, String> labelsByIndexes = new HashMap<>();

    public static final DefaultListModel model = new DefaultListModel();

    public MainWindow(String title) {
        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
        JLabel accountsLabel = new JLabel("Accounts (double click)");
        accountsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(accountsLabel);

        // accounts list
        // get the labels from the accounts and show them in the list
        for (int i = 0; i < JSONManager.getAccounts().size(); i++) {
            Account account = JSONManager.getAccounts().get(i);
            labelsByIndexes.put(i, account.getUsername());
            model.addElement(account.getUsername());
        }
        JList accountList = new JList(model);
        DefaultListCellRenderer renderer = (DefaultListCellRenderer) accountList.getCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.CENTER);
        accountList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        accountList.setLayoutOrientation(JList.VERTICAL);
        JScrollPane scrollBar = new JScrollPane(accountList);
        scrollBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(scrollBar);

        // new account button
        JButton newAccountButton = new JButton("New Account");
        newAccountButton.addActionListener(e -> {
            JFrame newAccountWindow = new NewAccountWindow("New Account");
            newAccountWindow.dispose();
        });

        newAccountButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        newAccountButton.setMaximumSize(new Dimension(300, newAccountButton.getMinimumSize().height));
        panel.add(newAccountButton);

        // delete account button
        JButton deleteAccountButton = new JButton("Delete Account");
        deleteAccountButton.addActionListener(e -> {
            JFrame deleteAccountWindow = new DeleteAccountWindow("Delete Account");
            deleteAccountWindow.dispose();
        });

        deleteAccountButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        deleteAccountButton.setMaximumSize(new Dimension(300, deleteAccountButton.getMinimumSize().height));
        panel.add(deleteAccountButton);

        // options button
        JButton optionsButton = new JButton("Options");
        optionsButton.addActionListener(e -> {
            JFrame options = new OptionsWindow("Options");
            options.dispose();
        });
        optionsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        optionsButton.setMaximumSize(new Dimension(300, optionsButton.getMinimumSize().height));
        panel.add(optionsButton);

        accountList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                JList list = (JList) evt.getSource();
                if (evt.getClickCount() == 2) {
                    int index = list.getSelectedIndex();
                    Account account = JSONManager.getAccounts().get(index);
                    SecretPrompt secretPrompt = new SecretPrompt("Passphrase", account);
                    secretPrompt.dispose();
                }
            }
        });

        frame.pack();
        frame.setSize(300, 400);
        frame.setVisible(true);
        frame.add(panel);
        frame.setLocationRelativeTo(null);
    }

    /**
     * This will "refresh" the accounts list after adding/deleting an account.
     * This is a dirty trick, but it works fine.
     * This also correctly sorts the accounts by the index from the accounts file.
     */
    public static void refreshAccountList() {
        model.removeAllElements();
        for (int i = 0; i < JSONManager.getAccounts().size(); i++) {
            Account account = JSONManager.getAccounts().get(i);
            labelsByIndexes.put(i, account.getUsername());
            model.addElement(account.getUsername());
        }
    }
}
