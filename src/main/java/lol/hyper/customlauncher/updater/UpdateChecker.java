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
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

public class UpdateChecker {
    static Logger logger = LogManager.getLogger(UpdateChecker.class);

    /**
     * Get all the releases.
     * @return List of all the JSONObjects.
     */
    public static ArrayList<JSONObject> getReleases() throws IOException {
        ArrayList<JSONObject> releases = new ArrayList<>();
        JSONArray remoteVersions = new JSONArray(readGitHubAPI());
        if (remoteVersions.isEmpty()) {
            logger.warn("GitHub's API returned empty!");
            return null;
        }
        // github will put the latest version at the 0 index
        for (int i = 0; i < remoteVersions.length(); i++) {
            releases.add(remoteVersions.getJSONObject(i));
        }
        return releases;
    }

    /** Downloads the latest version of the launcher from GitHub. */
    public static void downloadLatestVersion() throws IOException {
        JSONObject latestVersionObj = getReleases().get(0);
        JSONArray assets = latestVersionObj.getJSONArray("assets");
        HashMap<String, URL> downloadURLs = new HashMap<>();
        for (int i = 0; i < latestVersionObj.length(); i++) {
            JSONObject downloadObj = assets.getJSONObject(0);
            URL downloadURL = new URL(downloadObj.getString("browser_download_url"));
            String downloadURLString = downloadURL.toString();
            String extension = downloadURLString.substring(downloadURLString.lastIndexOf(".") + 1);
            if (extension.equalsIgnoreCase("exe")) {
                downloadURLs.put("windows", downloadURL);
            }
            if (extension.equalsIgnoreCase("linux")) {
                downloadURLs.put("linux", downloadURL);
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
        File output = new File(fileName);
        FileUtils.copyURLToFile(finalURL, output);

        // extract the tar.gz release file into the installation dir
        if (SystemUtils.IS_OS_LINUX) {
            logger.info("Extracting " + output + " to " + System.getProperty("user.dir"));
            decompress(fileName, output);
            FileUtils.delete(output);
        }
    }

    /**
     * Get the latest release notes.
     * @return String with the notes.
     */
    public static String getReleaseNotes() {
        JSONArray remoteVersions;
        try {
            remoteVersions = new JSONArray(readGitHubAPI());
        } catch (IOException e) {
            logger.error("Unable to check for updates!", e);
            return null;
        }
        if (remoteVersions.isEmpty()) {
            logger.warn("GitHub's API returned empty!");
            return null;
        }
        // github will put the latest version at the 0 index
        JSONObject latestVersionObj = remoteVersions.getJSONObject(0);
        return latestVersionObj.getString("body");

    }

    /**
     * Reads the GitHub API of the project.
     *
     * @return A JSONArray with all the info.
     */
    private static JSONArray readGitHubAPI() throws IOException {
        String remoteRaw = null;
        URL url =
                new URL("https://api.github.com/repos/hyperdefined/CustomLauncherRewrite/releases");
        URLConnection conn = url.openConnection();
        conn.setRequestProperty(
                "User-Agent",
                "CustomLauncherRewrite https://github.com/hyperdefined/CustomLauncherRewrite");
        conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
        conn.connect();

        try (InputStream in = conn.getInputStream()) {
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            remoteRaw = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            reader.close();
        } catch (IOException e) {
            logger.error("Unable to check for updates!", e);
        }

        if (remoteRaw == null || remoteRaw.length() == 0) {
            logger.warn(
                    "Unable to find latest version from GitHub. The string either returned null or empty.");
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
        String[] command = {"cmd", "/c", "CustomLauncherRewrite-" + newVersion + ".exe"};
        ProcessBuilder pb = new ProcessBuilder(command);
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
     * Extract the compressed tar.gz to an output
     *
     * @param temp The temp file's name that was downloaded.
     * @param output The output file.
     */
    public static void decompress(String temp, File output) throws IOException {
        File tempFile = new File(temp);
        TarArchiveInputStream in =
                new TarArchiveInputStream(new BufferedInputStream(new FileInputStream(tempFile)));
        FileOutputStream out = new FileOutputStream(output);
        try (in;
                out) {
            IOUtils.copy(in, out);
        }
    }

    /**
     * Get how many builds behind.
     * @param currentVersion The current version.
     * @return How many builds behind.
     */
    public static int getBuildsBehind(String currentVersion) throws IOException {
        ArrayList<JSONObject> releases = getReleases();
        for (int i = 0; i < releases.size(); i++) {
            JSONObject temp = releases.get(i);
            String version = temp.getString("tag_name");
            if (currentVersion.equalsIgnoreCase(version)) {
                return i;
            }
        }
        return -1;
    }
}
