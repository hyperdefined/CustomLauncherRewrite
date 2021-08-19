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

package lol.hyper.customlauncher.setup;

import lol.hyper.customlauncher.accounts.JSONManager;

import javax.swing.*;
import java.io.File;

public class FirstSetup extends JFrame {

    public FirstSetup() {
        JFrame frame = new JFrame();
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        JOptionPane.showMessageDialog(
                frame,
                "Welcome to CustomLauncherRewrite. I am going to first try and locate your TTR install directory.");

        final String installPathTest = "XX:\\Program Files (x86)\\Toontown Rewritten";
        String finalInstallPath = null;

        for (char alphabet = 'A'; alphabet <= 'Z'; alphabet++) {
            // we make a temp string so we don't replace the template.
            String temp = installPathTest.replace("XX", String.valueOf(alphabet));
            File location = new File(temp);
            if (location.exists()) {
                finalInstallPath = temp;
            }
        }
        if (finalInstallPath == null) {
            JOptionPane.showMessageDialog(
                    frame, "We are unable to find your install directory. You can set this directory in the settings.");
        } else {
            JOptionPane.showMessageDialog(frame, "We found your install directory. Everything is good to go!");
            JSONManager.editConfig("ttrInstallLocation", finalInstallPath);
        }
    }
}
