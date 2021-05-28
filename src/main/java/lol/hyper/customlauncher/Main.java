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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {

    public static final String DEFAULT_INSTALL = "C:\\Program Files (x86)\\Toontown Rewritten";
    public static String pathToUse;

    public static void main(String[] args) throws IOException {
        if (!JSONManager.configPath.toFile().exists()) {
            Files.createDirectory(JSONManager.configPath);
        }
        // create the default files
        // config.json with default values
        // accounts.json with no accounts
        if (!JSONManager.configFile.exists()) {
            JSONObject newOptions = new JSONObject();
            newOptions.put("ttrInstallLocation", DEFAULT_INSTALL);
            newOptions.put("autoCheckTTRUpdates", true);
            JSONManager.writeFile(newOptions, JSONManager.configFile);
        }
        if (!JSONManager.accountsFile.exists()) {
            JSONObject newAccounts = new JSONObject();
            JSONManager.writeFile(newAccounts, JSONManager.accountsFile);
        }

        JSONObject optionsFile = JSONManager.readFile(JSONManager.configFile);

        // check the config installation path
        // if it's not valid, use default
        if (!Paths.get(optionsFile.getString("ttrInstallLocation")).toFile().exists()) {
            JFrame invalidPath = new InvalidPath("Error");
            invalidPath.dispose();
            pathToUse = DEFAULT_INSTALL;
        } else {
            pathToUse = optionsFile.getString("ttrInstallLocation");
        }

        if (optionsFile.getBoolean("autoCheckTTRUpdates")) {
            JFrame updater = new Updater("Updater", Paths.get(pathToUse));
            updater.dispose();
        }
        JFrame mainWindow = new MainWindow("Launcher");
        mainWindow.dispose();
    }
}
