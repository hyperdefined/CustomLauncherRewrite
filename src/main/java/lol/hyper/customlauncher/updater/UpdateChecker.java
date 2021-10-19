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

import lol.hyper.customlauncher.generic.ErrorWindow;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class UpdateChecker {

    /**
     * Gets the latest version from GitHub.
     *
     * @return The latest version as a string.
     */
    public static String getLatestVersion() throws IOException {
        Logger logger = LogManager.getLogger(UpdateChecker.class);
        JSONArray remoteVersions = new JSONArray(readGitHubAPI());
        if (remoteVersions.isEmpty()) {
            logger.warn("GitHub's API returned empty!");
            return null;
        }
        // github will put the latest version at the 0 index
        JSONObject latestVersionObj = remoteVersions.getJSONObject(0);

        return latestVersionObj.getString("tag_name");
    }

    /**
     * Downloads the latest version of the launcher from GitHub.
     */
    public static void downloadLatestVersion() throws IOException {
        Logger logger = LogManager.getLogger(UpdateChecker.class);
        JSONArray remoteVersions = new JSONArray(readGitHubAPI());
        if (remoteVersions.isEmpty()) {
            logger.warn("GitHub's API returned empty!");
            return;
        }
        // github will put the latest version at the 0 index
        JSONObject latestVersionObj = remoteVersions.getJSONObject(0);
        JSONArray assets = latestVersionObj.getJSONArray("assets");
        JSONObject downloadObj = assets.getJSONObject(0);
        URL downloadURL = new URL(downloadObj.getString("browser_download_url"));
        logger.info("Downloading new version from " + downloadURL);
        FileUtils.copyURLToFile(downloadURL, new File(downloadObj.getString("name")));
    }

    /**
     * Reads the GitHub API of the project.
     *
     * @return A JSONArray with all the info.
     */
    private static JSONArray readGitHubAPI() throws IOException {
        Logger logger = LogManager.getLogger(UpdateChecker.class);
        String remoteRaw = null;
        URL url = new URL("https://api.github.com/repos/hyperdefined/CustomLauncherRewrite/releases");
        URLConnection conn = url.openConnection();
        conn.setRequestProperty(
                "User-Agent", "CustomLauncherRewrite https://github.com/hyperdefined/CustomLauncherRewrite");
        conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
        conn.connect();

        try (InputStream in = conn.getInputStream()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            remoteRaw = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            reader.close();
        } catch (IOException e) {
            logger.error("Unable to check for updates!", e);
        }

        if (remoteRaw == null || remoteRaw.length() == 0) {
            logger.warn("Unable to find latest version from GitHub. The string either returned null or empty.");
            return null;
        }
        return new JSONArray(remoteRaw);
    }

    /**
     * Launches the new version of the launcher that was downloaded.
     *
     * @param newVersion Version to launch.
     */
    public static void launchNewVersion(String newVersion) {
        Logger logger = LogManager.getLogger(UpdateChecker.class);
        String[] command = {"cmd", "/c", "CustomLauncherRewrite-" + newVersion + ".exe"};
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(new File(System.getProperty("user.dir")));
        try {
            Process p = pb.start();
            p.getInputStream().close();
        } catch (IOException e) {
            logger.error("Unable to launch game!", e);
            JFrame errorWindow = new ErrorWindow("Unable to new version!.\n" + e.getClass().getCanonicalName() + ": " + e.getMessage());
            errorWindow.dispose();
        }
    }
}
