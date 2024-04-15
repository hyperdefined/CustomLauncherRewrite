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

package lol.hyper.customlauncher.releasenotes;

import lol.hyper.customlauncher.CustomLauncherRewrite;
import lol.hyper.customlauncher.tools.JSONUtils;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;

public class ReleaseNotesWindow extends JFrame {

    /**
     * The text area for the window.
     */
    private final JTextPane textArea;

    /**
     * Creates a release note window to display a single update.
     *
     * @param gameUpdate The game update to show.
     */
    public ReleaseNotesWindow(GameUpdate gameUpdate) {
        setTitle(gameUpdate.version());
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception exception) {
            exception.printStackTrace();
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

        JSONObject notesJSON = JSONUtils.requestJSON("https://www.toontownrewritten.com/api/releasenotes/" + gameUpdate.id());
        if (notesJSON == null) {
            return;
        }
        String notes = notesJSON.getString("body");
        formatContent(notes);

        setLocationRelativeTo(null);
        pack();
        setVisible(true);
    }

    /**
     * Formats the content into proper HTML lists and headings.
     *
     * @param content The content to format.
     */
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
