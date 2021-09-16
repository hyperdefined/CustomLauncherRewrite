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

import lol.hyper.customlauncher.invasiontracker.InvasionTracker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class UpdateChecker {

    /**
     * Check for updates to the program.
     * @return True if there is an update.
     */
    public static boolean checkForUpdates(String currentVersion) throws IOException {
        Logger logger = LogManager.getLogger(UpdateChecker.class);
        String remoteVersion = null;
        URL url = new URL("https://raw.githubusercontent.com/hyperdefined/CustomLauncherRewrite/master/version");
        URLConnection conn = url.openConnection();
        conn.setRequestProperty(
                "User-Agent", "CustomLauncherRewrite https://github.com/hyperdefined/CustomLauncherRewrite");
        conn.connect();
        BufferedReader serverResponse = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        serverResponse.close();

        try (InputStream in = url.openStream()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            remoteVersion = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            reader.close();
        } catch (IOException e) {
            logger.error(e);
        }

        logger.info("Current version: " + currentVersion);
        logger.info("Latest version: " + remoteVersion);

        if (remoteVersion == null || remoteVersion.equalsIgnoreCase("")) {
            logger.warn("Unable to find latest version from GitHub. The string either returned null or empty.");
            return false;
        }

        return !remoteVersion.equals(currentVersion);
    }
}
