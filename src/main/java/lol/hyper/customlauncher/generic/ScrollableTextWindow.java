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

package lol.hyper.customlauncher.generic;

import lol.hyper.customlauncher.CustomLauncherRewrite;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class ScrollableTextWindow extends JFrame {

    private final JTextPane textArea;

    public final Logger logger = LogManager.getLogger(this);

    public ScrollableTextWindow(String title, String text) {
        setTitle(title);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        setIconImage(CustomLauncherRewrite.icon);
        setPreferredSize(new Dimension(600, 500));

        textArea = new JTextPane();
        textArea.setEditable(false);
        textArea.setContentType("text/html");

        JScrollPane scrollPane = new JScrollPane(textArea);
        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        verticalScrollBar.setValue(verticalScrollBar.getMinimum());
        getContentPane().add(scrollPane);

        formatContent(text);

        setLocationRelativeTo(null);
        pack();
        setVisible(true);
    }

    private void formatContent(String content) {
        String[] contentLines = content.split(System.lineSeparator());
        StringBuilder htmlContent = new StringBuilder();
        for (String line : contentLines) {
            String cleanedLine = line.trim();
            if (cleanedLine.matches("^\\*\\s?(.*)")) {
                // lines that start with * convert to list
                cleanedLine = cleanedLine.replaceAll("^\\*\\s?(.*)", "<li>$1</li>");
            } else if (cleanedLine.matches("^=(.*)$")) {
                // lines that start with = convert to header
                cleanedLine = cleanedLine.replaceAll("^=(.*)$", "<h1>$1</h1>");
            }
            htmlContent.append(cleanedLine).append(System.lineSeparator());
        }

        textArea.setText("<html><body><ul>" + htmlContent + "</ul></body></html>");
    }
}
