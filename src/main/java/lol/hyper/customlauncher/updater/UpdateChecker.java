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
import lol.hyper.customlauncher.tools.ExceptionWindow;
import lol.hyper.customlauncher.tools.OSDetection;
import lol.hyper.customlauncher.tools.PopUpWindow;
import lol.hyper.githubreleaseapi.GitHubRelease;
import lol.hyper.githubreleaseapi.GitHubReleaseAPI;
import lol.hyper.githubreleaseapi.ReleaseNotFoundException;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;

public class UpdateChecker {

    /**
     * GitHubReleaseAPI instance.
     */
    private GitHubReleaseAPI api;
    /**
     * The UpdateChecker logger.
     */
    private final Logger logger = LogManager.getLogger(this);

    /**
     * Creates an UpdateChecker instance.
     *
     * @param currentVersion The current version of the program.
     */
    public UpdateChecker(String currentVersion) {
        try {
            this.api = new GitHubReleaseAPI("CustomLauncherRewrite", "hyperdefined");
        } catch (IOException exception) {
            api = null;
            logger.error("Unable to look for updates!", exception);
            new ExceptionWindow(exception);
        }
        if (api != null) {
            checkForUpdate(currentVersion);
        }
    }

    /**
     * Check for updates.
     *
     * @param currentVersion The current version of the program.
     */
    private void checkForUpdate(String currentVersion) {
        logger.info("Checking for updates");
        String latestVersion = api.getLatestVersion().getTagVersion();
        GitHubRelease current;
        try {
            current = api.getReleaseByTag(currentVersion);
        } catch (ReleaseNotFoundException exception) {
            logger.error("Current version does not exist on GitHub!");
            new PopUpWindow(null, "It looks like you're running a version not present on GitHub.\nThis is the case if you're running in a dev environment!");
            return;
        }
        logger.info("Latest version is {}", latestVersion);
        int behind = api.getBuildsBehind(current);
        StringBuilder updates = new StringBuilder();
        // if the user is 1 or more build behind, ask to update
        if (behind > 0) {
            JTextArea textArea = new JTextArea();
            JScrollPane scrollPane = new JScrollPane(textArea);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            scrollPane.setPreferredSize(new Dimension(500, 500));
            updates.append("You are running an outdated version! You are running ").append(currentVersion).append(" currently.");
            updates.append(" Would you like to update?\n\n");
            for (int i = behind - 1; i >= 0; i--) {
                String tag = api.getAllReleases().get(i).getTagVersion();
                updates.append("----------------------------------------\nVersion: ").append(tag).append("\n").append(api.getReleaseByTag(tag).getReleaseNotes()).append("\n");
            }
            textArea.setText(updates.toString());
            logger.info("A new version is available! Version: {}", latestVersion);

            int dialogResult = JOptionPane.showConfirmDialog(null, scrollPane, "Updates", JOptionPane.YES_NO_OPTION);
            if (dialogResult == JOptionPane.YES_OPTION) {
                // download the latest version and run it
                downloadLatestVersion();
                launchNewVersion(latestVersion);
                System.exit(0);
            }
        } else {
            logger.info("Running latest version");
        }
    }

    /**
     * Downloads the latest version of the launcher from GitHub.
     */
    private void downloadLatestVersion() {
        if (api.getAllReleases().isEmpty()) {
            logger.error("Unable to look for updates!");
            logger.error("getAllReleases() is empty: {}", api.getAllReleases().isEmpty());
            new PopUpWindow(null, "Unable to look for updates! Check the log for more information.");
            return;
        }

        GitHubRelease release = api.getLatestVersion();

        if (OSDetection.isWindows()) {
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
                logger.error("Unable to find Windows file URL {} in assets!", newVersionName);
                new PopUpWindow(null, "Unable to find Windows file " + newVersionName + " in assets!");
                return;
            }

            logger.info("Downloading new version from {}", finalDownload);
            File output = new File(newVersionName);
            try {
                FileUtils.copyURLToFile(finalDownload.toURL(), output);
            } catch (IOException exception) {
                logger.error("Unable to download file from {}", finalDownload, exception);
                new ExceptionWindow(exception);
                return;
            }
            return;
        }

        // extract the tar.gz release file into the installation dir
        if (OSDetection.isLinux()) {
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
                logger.error("Unable to find Linux file URL {} in assets!", newVersionName);
                new PopUpWindow(null, "Unable to find Linux file " + newVersionName + " in assets!");
                return;
            }

            logger.info("Downloading new version from {}", finalDownload);
            File output = new File(newVersionName);
            try {
                FileUtils.copyURLToFile(finalDownload.toURL(), output);
            } catch (IOException exception) {
                logger.error("Unable to download file from {}", finalDownload, exception);
                new ExceptionWindow(exception);
                return;
            }

            logger.info("Extracting {} to {}", output, System.getProperty("user.dir"));
            decompress(newVersionName);
            try {
                FileUtils.delete(output);
            } catch (IOException exception) {
                logger.error("Unable to delete file {}", output, exception);
                new ExceptionWindow(exception);
            }
        }
    }

    /**
     * Launches the new version of the launcher that was downloaded.
     *
     * @param newVersion New version to launch.
     */
    private void launchNewVersion(String newVersion) {
        ProcessBuilder pb = new ProcessBuilder();
        if (OSDetection.isWindows()) {
            String[] windowsCommand = {"cmd", "/c", "CustomLauncherRewrite-" + newVersion + ".exe", "--remove-old", CustomLauncherRewrite.getVersion()};
            pb.command(windowsCommand);
        } else {
            pb.command("./run.sh");

            // delete the old version
            File current = new File(System.getProperty("user.dir") + File.separator + "CustomLauncherRewrite-" + CustomLauncherRewrite.getVersion() + ".jar");
            try {
                Files.delete(current.toPath());
            } catch (IOException exception) {
                logger.error("Unable to launch new version!", exception);
                new ExceptionWindow(exception);
            }
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
     * @param downloadedFile The temp file's name that was downloaded.
     */
    private void decompress(String downloadedFile) {
        try (FileInputStream fileInputStream = new FileInputStream(downloadedFile); BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream); GzipCompressorInputStream gzipInputStream = new GzipCompressorInputStream(bufferedInputStream); TarArchiveInputStream tarArchiveInputStream = new TarArchiveInputStream(gzipInputStream)) {

            TarArchiveEntry entry;
            while ((entry = tarArchiveInputStream.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    new File(System.getProperty("user.dir"), entry.getName()).mkdirs();
                } else {
                    File outputFile = new File(System.getProperty("user.dir"), entry.getName());
                    try (OutputStream outputStream = new FileOutputStream(outputFile)) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = tarArchiveInputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                    }
                }
            }
        } catch (IOException exception) {
            logger.error("Unable to decompress file {}", downloadedFile, exception);
            new ExceptionWindow(exception);
        }
    }
}
