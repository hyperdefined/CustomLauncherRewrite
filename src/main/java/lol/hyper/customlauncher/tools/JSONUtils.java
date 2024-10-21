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

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.stream.Collectors;

public class JSONUtils {
    /**
     * The JSONUtils logger.
     */
    private static final Logger logger = LogManager.getLogger(JSONUtils.class);

    /**
     * Read contents of a file.
     *
     * @param file The file to read.
     * @return The data from the file.
     */
    public static String readFile(File file) {
        logger.info("Reading file: {}", file.getAbsolutePath());
        byte[] encoded = new byte[0];
        try {
            encoded = Files.readAllBytes(file.toPath());
        } catch (IOException exception) {
            logger.error("Unable to read file {}", file, exception);
            new ExceptionWindow(exception);
        }
        return new String(encoded, StandardCharsets.UTF_8);
    }

    /**
     * Write a file.
     *
     * @param data The data to write. This will be turned into a String.
     * @param file The file to write to.
     */
    public static void writeFile(Object data, File file) {
        logger.info("Writing file: {}", file.getAbsolutePath());
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(data.toString());
            writer.close();
        } catch (IOException exception) {
            logger.error("Unable to write file {}", file, exception);
            new ExceptionWindow(exception);
        }
    }

    /**
     * Get a JSONObject from a URL.
     *
     * @param url The URL to get JSON from.
     * @return The response JSONObject. Returns null if there was some issue.
     */
    public static JSONObject requestJSON(String url) {
        logger.info("Fetching url: {}", url);
        String rawJSON;
        try {
            URLConnection conn = new URI(url).toURL().openConnection();
            conn.setRequestProperty("User-Agent", CustomLauncherRewrite.userAgent);
            conn.connect();

            InputStream in = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            rawJSON = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            reader.close();

        } catch (Exception exception) {
            logger.error("Unable to read URL {}", url, exception);
            new ExceptionWindow(exception);
            return null;
        }
        if (rawJSON.isEmpty()) {
            logger.error("Read JSON from {} returned an empty string!", url);
            return null;
        }
        return new JSONObject(rawJSON);
    }

    /**
     * Get a JSONArray from a URL.
     *
     * @param url The URL to get JSONArray from.
     * @return The response JSONArray. Returns null if there was some issue.
     */
    public static JSONArray requestJSONArray(String url) {
        logger.info("Fetching url: {}", url);
        String rawJSON;
        try {
            URLConnection conn = new URI(url).toURL().openConnection();
            conn.setRequestProperty("User-Agent", CustomLauncherRewrite.userAgent);
            conn.connect();

            InputStream in = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            rawJSON = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            reader.close();

        } catch (Exception exception) {
            logger.error("Unable to read URL {}", url, exception);
            new ExceptionWindow(exception);
            return null;
        }

        if (rawJSON.isEmpty()) {
            logger.error("Read JSON from {} returned an empty string!", url);
            return null;
        }
        return new JSONArray(rawJSON);
    }
}
