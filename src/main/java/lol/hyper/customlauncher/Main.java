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

package lol.hyper.customlauncher;

import lol.hyper.customlauncher.accounts.JSONManager;
import lol.hyper.customlauncher.accounts.windows.MainWindow;
import lol.hyper.customlauncher.updater.InvalidPath;
import lol.hyper.customlauncher.updater.Updater;
import org.json.JSONObject;

import javax.swing.*;
import java.nio.file.Paths;

public class Main {

    public static final String DEFAULT_INSTALL = "C:\\Program Files (x86)\\Toontown Rewritten";
    public static String pathToUse;

    public static void main(String[] args) {
        JSONObject optionsFile = JSONManager.readFile(JSONManager.configFile);
        // check to see if the default ttr install is there
        // if not, use the path in the config
        if (!Paths.get(DEFAULT_INSTALL).toFile().exists()) {
            pathToUse = optionsFile.getString("ttrInstallLocation");
            // check the config path
            if (!Paths.get(pathToUse).toFile().exists()) {
                InvalidPath invalidPath = new InvalidPath("Error");
                System.exit(1);
            }
        } else {
            pathToUse = DEFAULT_INSTALL;
        }
        if (optionsFile.getBoolean("autoCheckTTRUpdates")) {
            JFrame updater = new Updater("Updater", Paths.get(pathToUse));
            updater.dispose();
        }
        JFrame mainWindow = new MainWindow("Launcher");
        mainWindow.dispose();
    }
}
