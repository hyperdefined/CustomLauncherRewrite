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

package lol.hyper.customlauncher.tools;

import lol.hyper.customlauncher.CustomLauncherRewrite;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class PopUpWindow extends JDialog {

    /**
     * Create a simple popup message.
     *
     * @param parent      The parent frame this is spawning from.
     * @param infoMessage The message to display on the window.
     */
    public PopUpWindow(Frame parent, String infoMessage) {
        super(parent, "Info", true);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        setIconImage(CustomLauncherRewrite.icon);
        setLayout(new BorderLayout());

        JPanel frame = new JPanel();
        frame.setLayout(new BorderLayout());
        frame.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel messageLabel = new JLabel(infoMessage);
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        messageLabel.setFont(messageLabel.getFont().deriveFont(Font.PLAIN, 15));

        frame.add(messageLabel, BorderLayout.CENTER);

        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(okButton);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        this.setResizable(false);

        setContentPane(frame);

        pack();
        setLocationRelativeTo(frame);
        Toolkit.getDefaultToolkit().beep();
        setVisible(true);
    }
}
