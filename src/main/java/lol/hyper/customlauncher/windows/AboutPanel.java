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

package lol.hyper.customlauncher.windows;

import lol.hyper.customlauncher.CustomLauncherRewrite;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;

public class AboutPanel extends JPanel {

    /**
     * The AboutPanel logger.
     */
    private final Logger logger = LogManager.getLogger(this);


    /**
     * Creates an AboutPanel.
     */
    public AboutPanel() {

        // GUI elements
        setLayout(new BorderLayout());

        JTextPane aboutBox = new JTextPane();
        aboutBox.setEditable(false);
        aboutBox.setContentType("text/html");

        String text = "<html>CustomLauncherRewrite " + CustomLauncherRewrite.version +
                "<br><hr>" + "CustomLauncherRewrite is an all purpose launcher for Toontown Rewritten. Created and maintained by hyperdefined.<br><br>" +
                "Contributors:<br>" + "- <a href=\"https://github.com/ahmouse15\">ahmouse15</a>" +
                "</html>";
        aboutBox.setText(text);

        aboutBox.addHyperlinkListener(event -> {
            if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                try {
                    Desktop.getDesktop().browse(event.getURL().toURI());
                } catch (IOException | URISyntaxException exception) {
                    logger.error("Unable to open URL", exception);
                }
            }
        });

        add(aboutBox);
    }
}
