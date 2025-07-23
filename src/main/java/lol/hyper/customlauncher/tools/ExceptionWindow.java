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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;

public class ExceptionWindow extends JFrame {

    /**
     * Create a popup window for exceptions.
     *
     * @param exceptionToDisplay The exception to display on the window.
     */
    public ExceptionWindow(Exception exceptionToDisplay) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception exception) {
            Logger logger = LogManager.getLogger(this);
            logger.error(exception);
        }
        // taken from https://stackoverflow.com/a/14011536
        StringBuilder sb = new StringBuilder(exceptionToDisplay.getClass().getCanonicalName());
        sb.append(": ");
        sb.append(exceptionToDisplay.getMessage());
        sb.append("\n");
        for (StackTraceElement ste : exceptionToDisplay.getStackTrace()) {
            sb.append(ste.toString());
            sb.append("\n");
        }
        JTextArea jta = new JTextArea(sb.toString());
        jta.setEditable(false);
        JScrollPane jsp = new JScrollPane(jta) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(480, 320);
            }
        };
        JOptionPane.showMessageDialog(this, jsp, "Error", JOptionPane.ERROR_MESSAGE);
        setLocationRelativeTo(null);
        dispose();
    }
}
