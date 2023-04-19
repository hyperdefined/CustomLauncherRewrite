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
import lol.hyper.customlauncher.windows.MainWindow;
import lol.hyper.customlauncher.ttrupdater.TTRUpdater;
import lol.hyper.customlauncher.updater.UpdateChecker;
import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Properties;

public class Main {

    public static String version;
    public static Logger logger;
    public static Image icon;
    public static String userAgent;

    public static void main(String[] args) throws IOException {
        // load the log4j2config
        System.setProperty("log4j.configurationFile", "log4j2config.xml");
        // load the version
        final Properties properties = new Properties();
        properties.load(Main.class.getClassLoader().getResourceAsStream("project.properties"));
        version = properties.getProperty("version");
        // log some basic info
        logger = LogManager.getLogger(Main.class);
        logger.info(
                System.getProperty("os.name")
                        + " "
                        + System.getProperty("sun.arch.data.model")
                        + "bit");
        logger.info("Program is starting.");
        logger.info("Running version " + version);
        logger.info("Current directory " + System.getProperty("user.dir"));

        userAgent =
                "CustomLauncherRewrite https://github.com/hyperdefined/CustomLauncherRewrite "
                        + version;

        // create the config folder
        final File configPath = new File("config");
        if (!configPath.exists()) {
            Files.createDirectory(configPath.toPath());
            logger.info("Creating config folder at " + configPath.getAbsolutePath());
        }

        ConfigHandler configHandler = new ConfigHandler();

        // create the ttr-files folder
        if (!ConfigHandler.INSTALL_LOCATION.exists()) {
            Files.createDirectory(ConfigHandler.INSTALL_LOCATION.toPath());
            logger.info(
                    "Creating TTR install folder at "
                            + ConfigHandler.INSTALL_LOCATION.getAbsolutePath());
            JSONObject options = new JSONObject();
            // TTR on linux has a bug with fullscreen mode
            // we set TTR to be windowed mode on first launch
            // the user can always change this
            if (SystemUtils.IS_OS_LINUX) {
                JSONObject videoSettings = new JSONObject();
                videoSettings.put("display-mode", "window");
                options.put("video", videoSettings);
            }
            // lower the game audio so the user doesn't die when it launches
            JSONObject audioSettings = new JSONObject();
            audioSettings.put("music-volume", 10);
            audioSettings.put("sfx-volume", 10);
            options.put("audio", audioSettings);
            JSONManager.writeFile(options, new File("ttr-files/settings.json"));
        }

        // load the icon
        InputStream iconStream = Main.class.getResourceAsStream("/icon.png");
        if (iconStream != null) {
            icon = ImageIO.read(iconStream);
        }

        // create accounts.json with no accounts
        if (!JSONManager.accountsFile.exists()) {
            JSONArray newAccounts = new JSONArray();
            JSONManager.writeFile(newAccounts, JSONManager.accountsFile);
            logger.info("Creating base accounts file...");
        }

        // this is used for removing old versions on Windows
        // passing "--remove-old <version>" will delete that version's exe
        // mainly for cleanup so there aren't 100 exes in the folder
        if (args.length >= 1) {
            String arg1 = args[0];
            if (arg1.equalsIgnoreCase("--remove-old")) {
                String oldVersion = args[1];
                Files.delete(new File("CustomLauncherRewrite-" + oldVersion + ".exe").toPath());
                logger.info("Deleting old version " + oldVersion);
            }
        }

        new UpdateChecker(version);

        File ttrFiles = new File("ttr-files");
        // we don't have ttr installed, run first setup
        if (!ttrFiles.exists()) {
            new FirstLaunch();
        }

        // run the TTR updater
        new TTRUpdater("Updater");

        // run the main window
        JFrame mainWindow = new MainWindow(configHandler);
        mainWindow.dispose();
    }
}
