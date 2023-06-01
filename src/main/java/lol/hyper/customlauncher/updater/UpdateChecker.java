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

import lol.hyper.customlauncher.CustomLauncherRewrite;
import lol.hyper.customlauncher.accounts.Accounts;
import lol.hyper.customlauncher.tools.ExceptionWindow;
import lol.hyper.customlauncher.tools.PopUpWindow;
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
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;

public class UpdateChecker {

    private GitHubReleaseAPI api;
    private final Logger logger = LogManager.getLogger(this);

    public UpdateChecker(String currentVersion) {
        try {
            this.api = new GitHubReleaseAPI("CustomLauncherRewrite", "hyperdefined");
        } catch (IOException exception) {
            api = null;
            logger.error("Unable to look for updates!", exception);
            new ExceptionWindow(exception);
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
            new PopUpWindow(null, "It looks like you're running a version not present on GitHub.\nThis is the case if you're running in a dev environment!");
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

                // hardcode this
                if (latestVersion.equals("1.9.0")) {
                    File accountsBackup = new File("config", "accounts-BACKUP.json");
                    try {
                        FileUtils.copyFile(Accounts.ACCOUNTS_FILE, accountsBackup);
                    } catch (IOException exception) {
                        logger.error("Unable to backup the accounts file!", exception);
                        new ExceptionWindow(exception);
                    }
                }

                launchNewVersion(latestVersion);
                System.exit(0);
            }
        }
    }

    /**
     * Downloads the latest version of the launcher from GitHub.
     */
    private void downloadLatestVersion() {
        if (api.getAllReleases() == null || api.getAllReleases().isEmpty()) {
            logger.error("Unable to look for updates!");
            logger.error("getAllReleases() is null" + (api.getAllReleases() == null));
            logger.error("getAllReleases() is empty" + (api.getAllReleases().isEmpty()));
            new PopUpWindow(null, "Unable to look for updates!");
            return;
        }

        GitHubRelease release = api.getLatestVersion();

        if (SystemUtils.IS_OS_WINDOWS) {
            String newVersionName = "CustomLauncherRewrite-" + release.getTagVersion() + ".exe";
            URI finalDownload = null;
            for (String url : release.getReleaseAssets()) {
                if (url.contains(newVersionName)) {
                    try {
                        finalDownload = new URI(url);
                    } catch (URISyntaxException exception) {
                        logger.error("Unable to download file!", exception);
                        new ExceptionWindow(exception);
                        return;
                    }
                }
            }
            if (finalDownload == null) {
                logger.error("Unable to find Windows file " + newVersionName + " in assets!");
                new PopUpWindow(null, "Unable to find Windows file " + newVersionName + " in assets!");
                return;
            }

            logger.info("Downloading new version from " + finalDownload);
            File output = new File(newVersionName);
            try {
                FileUtils.copyURLToFile(finalDownload.toURL(), output);
            } catch (IOException exception) {
                logger.error("Unable to download file from " + finalDownload, exception);
                new ExceptionWindow(exception);
                return;
            }
            return;
        }

        // extract the tar.gz release file into the installation dir
        if (SystemUtils.IS_OS_LINUX) {
            String newVersionName = "CustomLauncherRewrite-" + release.getTagVersion() + ".tar.gz";
            URI finalDownload = null;
            for (String url : release.getReleaseAssets()) {
                if (url.contains(newVersionName)) {
                    try {
                        finalDownload = new URI(url);
                    } catch (URISyntaxException exception) {
                        logger.error("Unable to download file!", exception);
                        new ExceptionWindow(exception);
                        return;
                    }
                }
            }
            if (finalDownload == null) {
                logger.error("Unable to find Windows file " + newVersionName + " in assets!");
                new PopUpWindow(null, "Unable to find Linux file " + newVersionName + " in assets!");
                return;
            }

            logger.info("Downloading new version from " + finalDownload);
            File output = new File(newVersionName);
            try {
                FileUtils.copyURLToFile(finalDownload.toURL(), output);
            } catch (IOException exception) {
                logger.error("Unable to download file from " + finalDownload, exception);
                new ExceptionWindow(exception);
                return;
            }

            logger.info("Extracting " + output + " to " + System.getProperty("user.dir"));
            decompress(newVersionName);
            try {
                FileUtils.delete(output);
            } catch (IOException exception) {
                logger.error("Unable to delete file " + output, exception);
                new ExceptionWindow(exception);
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
                CustomLauncherRewrite.version
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
                                    + CustomLauncherRewrite.version
                                    + ".jar");
            try {
                Files.delete(current.toPath());
            } catch (IOException exception) {
                logger.error("Unable to launch new version!", exception);
                new ExceptionWindow(exception);
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
            new ExceptionWindow(exception);
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
            logger.error("Unable to extract release file!", exception);
            new ExceptionWindow(exception);
            return;
        }
        int exitCode = 0;
        try {
            exitCode = process.waitFor();
        } catch (InterruptedException exception) {
            logger.error("Unable to extract release file!", exception);
            new ExceptionWindow(exception);
        }
        if (exitCode == 0) {
            logger.info("Extracted " + temp + "!");
        } else {
            logger.error("Unable to extract release file! Returned exit code " + exitCode);
            new PopUpWindow(null, "Unable to extract release file!");
        }
    }
}
