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
import lol.hyper.customlauncher.generic.ErrorWindow;
import lol.hyper.customlauncher.updater.Updater;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {

    public static final String DEFAULT_INSTALL = "C:\\Program Files (x86)\\Toontown Rewritten";
    public static String pathToUse;
    public static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) throws IOException {
        if (!JSONManager.configPath.toFile().exists()) {
            Files.createDirectory(JSONManager.configPath);
            logger.warn("Config path was not found, creating directory...");
        }
        // create the default files
        // config.json with default values
        // accounts.json with no accounts
        if (!JSONManager.configFile.exists()) {
            JSONObject newOptions = new JSONObject();
            newOptions.put("ttrInstallLocation", DEFAULT_INSTALL);
            newOptions.put("autoCheckTTRUpdates", true);
            JSONManager.writeFile(newOptions, JSONManager.configFile);
            logger.info("Creating base config file...");
        }
        if (!JSONManager.accountsFile.exists()) {
            JSONArray newAccounts = new JSONArray();
            JSONManager.writeFile(newAccounts, JSONManager.accountsFile);
            logger.info("Creating base accounts file...");
        }

        JSONObject optionsFile = new JSONObject(JSONManager.readFile(JSONManager.configFile));

        // automatically convert the old format to the new one
        char firstChar = JSONManager.readFile(JSONManager.accountsFile).charAt(0);
        if (firstChar == '{') {
            JSONManager.convertToNewFormat();
            Main.logger.info("Converting account storage to JSONArray format.");
        }

        // check the config installation path
        Main.logger.info("ttrInstallLocation = " + optionsFile.getString("ttrInstallLocation"));
        if (!Paths.get(optionsFile.getString("ttrInstallLocation")).toFile().exists()) {
            Main.logger.warn("ttrInstallLocation does not exist. Is the game installed here?");
            JFrame errorWindow = new ErrorWindow("Unable to find your TTR install directory. We won't be able to check for TTR updates nor run the game.");
            errorWindow.dispose();
            pathToUse = null;
        }

        Main.logger.info("autoCheckTTRUpdates = " + optionsFile.getBoolean("autoCheckTTRUpdates"));
        if (optionsFile.getBoolean("autoCheckTTRUpdates")) {
            if (pathToUse != null) {
                JFrame updater = new Updater("Updater", Paths.get(pathToUse));
                updater.dispose();
            }
        }
        JFrame mainWindow = new MainWindow("Launcher");
        mainWindow.dispose();
    }
}
