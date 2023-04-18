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

package lol.hyper.customlauncher.updater;

import lol.hyper.customlauncher.Main;
import lol.hyper.customlauncher.generic.ErrorWindow;
import lol.hyper.githubreleaseapi.GitHubRelease;
import lol.hyper.githubreleaseapi.GitHubReleaseAPI;
import lol.hyper.githubreleaseapi.ReleaseNotFoundException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;import java.net.URISyntaxException;import java.net.URL;
import java.nio.file.Files;
import java.util.HashMap;import java.util.concurrent.ConcurrentHashMap;

public class UpdateChecker {

    private GitHubReleaseAPI api;
    private final Logger logger = LogManager.getLogger(this);

    public UpdateChecker(String currentVersion) {
        try {
            this.api = new GitHubReleaseAPI("CustomLauncherRewrite", "hyperdefined");
        } catch (IOException exception) {
            api = null;
            logger.error("Unable to look for updates!", exception);
            ErrorWindow errorWindow = new ErrorWindow(exception);
            errorWindow.dispose();
        }
        checkForUpdate(currentVersion);
    }

    private void checkForUpdate(String currentVersion) {
        // if the api is broken, don't even bother
        if (api == null) {
            return;
        }
        String latestVersion = api.getLatestVersion().getTagVersion();
        GitHubRelease current;
        try {
            current = api.getReleaseByTag(currentVersion);
        } catch (ReleaseNotFoundException exception) {
            logger.error("Current version does not exist on GitHub!");
            ErrorWindow errorWindow = new ErrorWindow("It looks like you're running a version not present on GitHub.\nThis is the case if you're running in a dev environment!");
            errorWindow.dispose();
            return;
        }
        int behind = api.getBuildsBehind(current);
        StringBuilder updates = new StringBuilder();
        // if the user is 1 or more build behind, ask to update
        if (behind > 0) {
            JTextArea textArea = new JTextArea();
            JScrollPane scrollPane = new JScrollPane(textArea);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            scrollPane.setPreferredSize(new Dimension(500, 500));
            updates.append("You are running an outdated version! You are running ")
                    .append(currentVersion)
                    .append(" currently.");
            updates.append(" Would you like to update?\n\n");
            for (int i = behind - 1; i >= 0; i--) {
                String tag = api.getAllReleases().get(i).getTagVersion();
                updates.append("----------------------------------------\nVersion: ")
                        .append(tag)
                        .append("\n")
                        .append(api.getReleaseByTag(tag).getReleaseNotes())
                        .append("\n");
            }
            textArea.setText(updates.toString());
            logger.info("A new version is available! Version: " + latestVersion);

            int dialogResult =
                    JOptionPane.showConfirmDialog(
                            null, scrollPane, "Updates", JOptionPane.YES_NO_OPTION);
            if (dialogResult == JOptionPane.YES_OPTION) {
                // download the latest version and run it
                downloadLatestVersion();
                launchNewVersion(latestVersion);
                System.exit(0);
            }
        }
    }

    /** Downloads the latest version of the launcher from GitHub. */
    private void downloadLatestVersion() {
        HashMap<String, URI> downloadURLs = new HashMap<>();
        if (api.getAllReleases() == null || api.getAllReleases().isEmpty()) {
            logger.error("Unable to look for updates!");
            logger.error("getAllReleases() is null" + (api.getAllReleases() == null));
            logger.error("getAllReleases() is empty" + (api.getAllReleases().isEmpty()));
            ErrorWindow errorWindow = new ErrorWindow("Unable to look for updates!");
            errorWindow.dispose();
            return;
        }

        GitHubRelease release = api.getLatestVersion();
        for (String url : release.getReleaseAssets()) {
            String extension = url.substring(url.lastIndexOf(".") + 1);
            URI downloadURL;
            try {
                downloadURL = new URI(url);
            } catch (URISyntaxException exception) {
                logger.error("Unable to look for updates! ", exception);
                ErrorWindow errorWindow = new ErrorWindow("Unable to look for updates!");
                errorWindow.dispose();
                return;
            }
            if (extension.equalsIgnoreCase("exe")) {
                downloadURLs.put("windows", downloadURL);
            }
            if (extension.equalsIgnoreCase("gz")) {
                downloadURLs.put("linux", downloadURL);
            }
        }

        URI finalURL = null;
        if (SystemUtils.IS_OS_WINDOWS) {
            finalURL = downloadURLs.get("windows");
        }
        if (SystemUtils.IS_OS_LINUX) {
            finalURL = downloadURLs.get("linux");
        }

        if (finalURL == null) {
            logger.warn("Unable to detect operating system!");
            return;
        }

        logger.info("Downloading new version from " + finalURL);
        String fileName = finalURL.toString().substring(finalURL.toString().lastIndexOf("/") + 1);
        logger.info(fileName);
        File output = new File(fileName);
        try {
            FileUtils.copyURLToFile(finalURL.toURL(), output);
        } catch (IOException exception) {
            logger.error("Unable to download file from " + finalURL, exception);
            ErrorWindow errorWindow = new ErrorWindow(exception);
            errorWindow.dispose();
            return;
        }

        // extract the tar.gz release file into the installation dir
        if (SystemUtils.IS_OS_LINUX) {
            logger.info("Extracting " + output + " to " + System.getProperty("user.dir"));
            decompress(fileName);
            try {
                FileUtils.delete(output);
            } catch (IOException exception) {
                logger.error("Unable to delete file " + output, exception);
                ErrorWindow errorWindow = new ErrorWindow(exception);
                errorWindow.dispose();
            }
        }
    }

    /**
     * Launches the new version of the launcher that was downloaded.
     *
     * @param newVersion Version to launch.
     */
    private void launchNewVersion(String newVersion) {
        String[] windowsCommand = {
            "cmd",
            "/c",
            "CustomLauncherRewrite-" + newVersion + ".exe",
            "--remove-old",
            Main.version
        };
        String linuxCommand = "./run.sh";
        ProcessBuilder pb = new ProcessBuilder();
        if (SystemUtils.IS_OS_LINUX) {
            pb.command(linuxCommand);

            // delete the old version
            File current =
                    new File(
                            System.getProperty("user.dir")
                                    + File.separator
                                    + "CustomLauncherRewrite-"
                                    + Main.version
                                    + ".jar");
            try {
                Files.delete(current.toPath());
            } catch (IOException exception) {
                logger.error("Unable to launch new version!", exception);
                ErrorWindow errorWindow = new ErrorWindow(exception);
                errorWindow.dispose();
            }
        }
        if (SystemUtils.IS_OS_WINDOWS) {
            pb.command(windowsCommand);
        }
        pb.directory(new File(System.getProperty("user.dir")));
        try {
            Process p = pb.start();
            p.getInputStream().close();
        } catch (IOException exception) {
            logger.error("Unable to launch new version!", exception);
            ErrorWindow errorWindow = new ErrorWindow(exception);
            errorWindow.dispose();
        }
    }

    /**
     * Extract the compressed tar.gz.
     *
     * @param temp The temp file's name that was downloaded.
     */
    private void decompress(String temp) {
        // TODO: Make this not use shell commands.
        ProcessBuilder builder = new ProcessBuilder();
        builder.command("tar", "-xvf", temp);
        builder.directory(new File(System.getProperty("user.dir")));
        Process process;
        try {
            process = builder.start();
        } catch (IOException exception) {
            logger.error("Unable to launch new version!", exception);
            ErrorWindow errorWindow = new ErrorWindow(exception);
            errorWindow.dispose();
            return;
        }
        int exitCode = 0;
        try {
            exitCode = process.waitFor();
        } catch (InterruptedException exception) {
            logger.error("Unable to launch new version!", exception);
            ErrorWindow errorWindow = new ErrorWindow(exception);
            errorWindow.dispose();
        }
        if (exitCode == 0) {
            logger.info("Extracted " + temp + "!");
        } else {
            logger.error("Unable to extract release file! Returned exit code " + exitCode);
            ErrorWindow errorWindow = new ErrorWindow("Unable to extract release file!");
            errorWindow.dispose();
        }
    }
}
