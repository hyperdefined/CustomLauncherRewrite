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
import lol.hyper.customlauncher.invasiontracker.InvasionTracker;
import lol.hyper.customlauncher.ttrupdater.TTRUpdater;
import lol.hyper.customlauncher.updater.UpdateChecker;
import lol.hyper.githubreleaseapi.GitHubRelease;
import lol.hyper.githubreleaseapi.GitHubReleaseAPI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class Main {

    public static String VERSION;
    public static Logger logger;
    public static Image icon;
    public static final File TTR_INSTALL_DIR = new File("ttr-files");

    public static void main(String[] args) throws IOException, InterruptedException {
        // load the log4j2config
        System.setProperty("log4j.configurationFile", "log4j2config.xml");
        // load the version
        final Properties properties = new Properties();
        properties.load(Main.class.getClassLoader().getResourceAsStream("project.properties"));
        VERSION = properties.getProperty("version");
        // log some basic info
        logger = LogManager.getLogger(Main.class);
        logger.info(System.getProperty("os.name"));
        logger.info("Program is starting.");
        logger.info("Running version " + VERSION);

        // create the config folder
        final File configPath = new File("config");
        if (!configPath.exists()) {
            Files.createDirectory(configPath.toPath());
            logger.info("Creating config folder at " + configPath.getAbsolutePath());
        }

        // create the ttr-files folder
        if (!TTR_INSTALL_DIR.exists()) {
            Files.createDirectory(TTR_INSTALL_DIR.toPath());
            logger.info("Creating TTR install folder at " + TTR_INSTALL_DIR.getAbsolutePath());
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

        // automatically convert the old format to the new one
        // probably not needed anymore
        char firstChar = JSONManager.readFile(JSONManager.accountsFile).charAt(0);
        if (firstChar == '{') {
            JSONManager.convertToNewFormat();
            Main.logger.info("Converting account storage to JSONArray format.");
        }

        GitHubReleaseAPI api = new GitHubReleaseAPI("CustomLauncherRewrite", "hyperdefined");
        GitHubRelease latest = api.getLatestVersion();
        String latestVersion = latest.getTagVersion();
        GitHubRelease current = api.getReleaseByTag(VERSION);
        int behind = api.getBuildsBehind(current);
        logger.info(current);
        UpdateChecker updateChecker = new UpdateChecker(api);
        if (!latestVersion.equals(VERSION)) {
            logger.info("A new version is available! Version: " + latestVersion);
            int dialogResult =
                    JOptionPane.showConfirmDialog(
                            null,
                            "You are currently "
                                    + behind
                                    + " versions behind. Latest version is: "
                                    + latestVersion
                                    + ".\nWould you like to update?\n\n"
                                    + latest.getReleaseNotes(),
                            "New Update",
                            JOptionPane.YES_NO_OPTION);
            if (dialogResult == JOptionPane.YES_OPTION) {
                updateChecker.downloadLatestVersion();
                int dialogResult2 =
                        JOptionPane.showConfirmDialog(
                                null,
                                "Version "
                                        + latestVersion
                                        + " was downloaded. Would you like to run this new version?",
                                "New Update",
                                JOptionPane.YES_NO_OPTION);
                if (dialogResult2 == JOptionPane.YES_OPTION) {
                    updateChecker.launchNewVersion(latestVersion);
                    System.exit(0);
                }
            }
        }

        // run the TTR updater
        JFrame updater = new TTRUpdater("Updater", Paths.get(TTR_INSTALL_DIR.getAbsolutePath()));
        updater.dispose();

        // run the main window
        JFrame mainWindow = new MainWindow("CLR " + VERSION, new InvasionTracker());
        mainWindow.dispose();
    }
}
