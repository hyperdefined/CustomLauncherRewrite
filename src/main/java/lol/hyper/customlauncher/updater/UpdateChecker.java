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
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.HashMap;

public class UpdateChecker {

    private final GitHubReleaseAPI api;
    private final Logger logger = LogManager.getLogger(UpdateChecker.class);

    public UpdateChecker(GitHubReleaseAPI api) {
        this.api = api;
    }

    /** Downloads the latest version of the launcher from GitHub. */
    public void downloadLatestVersion() throws IOException, InterruptedException {
        HashMap<String, URL> downloadURLs = new HashMap<>();
        if (api.getAllReleases() == null || api.getAllReleases().isEmpty()) {
            JFrame errorWindow = new ErrorWindow("Unable to look for updates!");
            errorWindow.dispose();
            return;
        }

        GitHubRelease release = api.getLatestVersion();
        for (String url : release.getReleaseAssets()) {
            String extension = url.substring(url.lastIndexOf(".") + 1);
            if (extension.equalsIgnoreCase("exe")) {
                downloadURLs.put("windows", new URL(url));
            }
            if (extension.equalsIgnoreCase("gz")) {
                downloadURLs.put("linux", new URL(url));
            }
        }

        URL finalURL = null;
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
        FileUtils.copyURLToFile(finalURL, output);

        // extract the tar.gz release file into the installation dir
        if (SystemUtils.IS_OS_LINUX) {
            logger.info("Extracting " + output + " to " + System.getProperty("user.dir"));
            decompress(fileName);
            FileUtils.delete(output);
        }
    }

    /**
     * Launches the new version of the launcher that was downloaded.
     *
     * @param newVersion Version to launch.
     */
    public void launchNewVersion(String newVersion) throws IOException {
        String[] windowsCommand = {"cmd", "/c", "CustomLauncherRewrite-" + newVersion + ".exe"};
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
                                    + Main.VERSION
                                    + ".jar");
            Files.delete(current.toPath());
        }
        if (SystemUtils.IS_OS_WINDOWS) {
            pb.command(windowsCommand);
        }
        pb.directory(new File(System.getProperty("user.dir")));
        try {
            Process p = pb.start();
            p.getInputStream().close();
        } catch (IOException e) {
            logger.error("Unable to launch new version!", e);
            JFrame errorWindow =
                    new ErrorWindow(
                            "Unable to launch new version!.\n"
                                    + e.getClass().getCanonicalName()
                                    + ": "
                                    + e.getMessage());
            errorWindow.dispose();
        }
    }

    /**
     * Extract the compressed tar.gz.
     *
     * @param temp The temp file's name that was downloaded.
     */
    private void decompress(String temp) throws IOException, InterruptedException {
        // TODO: Make this not use shell commands.
        ProcessBuilder builder = new ProcessBuilder();
        builder.command("tar", "-xvf", temp);
        builder.directory(new File(System.getProperty("user.dir")));
        Process process = builder.start();
        int exitCode = process.waitFor();
        if (exitCode == 0) {
            logger.info("Extracted " + temp + "!");
        } else {
            JFrame errorWindow = new ErrorWindow("Unable to extract release file.");
            errorWindow.dispose();
        }
    }
}
