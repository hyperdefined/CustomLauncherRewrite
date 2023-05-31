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

package lol.hyper.customlauncher.tools;

import lol.hyper.customlauncher.CustomLauncherRewrite;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.stream.Collectors;

public class JSONManager {
    private static final Logger logger = LogManager.getLogger(JSONManager.class);

    /**
     * Read contents of a file.
     *
     * @return Contents of a file into a String.
     */
    public static String readFile(File file) {
        byte[] encoded = new byte[0];
        try {
            encoded = Files.readAllBytes(file.toPath());
        } catch (IOException exception) {
            logger.error("Unable to read file " + file, exception);
            new ErrorWindow(exception);
        }
        return new String(encoded, StandardCharsets.UTF_8);
    }

    /**
     * Write data to JSON file.
     *
     * @param json Data to write to file.
     */
    public static void writeFile(Object json, File file) {
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(json.toString());
            writer.close();
        } catch (IOException exception) {
            logger.error("Unable to write file " + file, exception);
            new ErrorWindow(exception);
        }
    }

    /**
     * Get a JSONObject from a URL.
     *
     * @param url The URL to get JSON from.
     * @return The response JSONObject.
     */
    public static JSONObject requestJSON(String url) {
        String rawJSON;
        try {
            URLConnection conn = new URL(url).openConnection();
            conn.setRequestProperty("User-Agent", CustomLauncherRewrite.userAgent);
            conn.connect();

            InputStream in = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            rawJSON = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            reader.close();

        } catch (IOException exception) {
            logger.error("Unable to read URL " + url, exception);
            new ErrorWindow(exception);
            return null;
        }

        if (rawJSON.isEmpty()) {
            logger.error("Read JSON from " + url + " returned an empty string!");
            return null;
        }
        return new JSONObject(rawJSON);
    }

    /**
     * Get a JSONArray from a URL.
     *
     * @param url The URL to get JSONArray from.
     * @return The response JSONArray.
     */
    public static JSONArray requestJSONArray(String url) {
        String rawJSON;
        try {
            URLConnection conn = new URL(url).openConnection();
            conn.setRequestProperty("User-Agent", CustomLauncherRewrite.userAgent);
            conn.connect();

            InputStream in = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            rawJSON = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            reader.close();

        } catch (IOException exception) {
            logger.error("Unable to read URL " + url, exception);
            new ErrorWindow(exception);
            return null;
        }

        if (rawJSON.isEmpty()) {
            logger.error("Read JSON from " + url + " returned an empty string!");
            return null;
        }
        return new JSONArray(rawJSON);
    }
}
