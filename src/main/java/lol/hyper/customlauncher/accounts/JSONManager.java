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

package lol.hyper.customlauncher.accounts;

import lol.hyper.customlauncher.Main;
import lol.hyper.customlauncher.generic.ErrorWindow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

public class JSONManager {

    public static final File accountsFile =
            Paths.get("config" + File.separator + "accounts.json").toFile();
    private static SecretKeySpec secretKey;
    private static final Logger logger = LogManager.getLogger(JSONManager.class);

    /**
     * Read contents of a file.
     *
     * @return Contents of a file.
     */
    public static String readFile(File file) {
        byte[] encoded = new byte[0];
        try {
            encoded = Files.readAllBytes(file.toPath());
        } catch (IOException exception) {
            logger.error("Unable to read file " + file, exception);
            JFrame errorWindow = new ErrorWindow(null, exception);
            errorWindow.dispose();
        }
        return new String(encoded, StandardCharsets.UTF_8);
    }

    /**
     * Write data to JSON file.
     *
     * @param json Data to write to file. This much be a JSON string.
     */
    public static void writeFile(Object json, File file) {
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(json.toString());
            writer.close();
        } catch (IOException exception) {
            logger.error("Unable to write file " + file, exception);
            JFrame errorWindow = new ErrorWindow(null, exception);
            errorWindow.dispose();
        }
    }

    public static void setKey(String myKey) {
        MessageDigest sha;
        try {
            byte[] key = myKey.getBytes(StandardCharsets.UTF_8);
            sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            secretKey = new SecretKeySpec(key, "AES");
        } catch (NoSuchAlgorithmException exception) {
            logger.error("Error while setting key!", exception);
        }
    }

    /**
     * Encrypt a string using a secret key.
     *
     * @param strToEncrypt String to encrypt.
     * @param secret Secret passphrase.
     * @return The encrypted string.
     */
    public static String encrypt(String strToEncrypt, String secret) {
        try {
            setKey(secret);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder()
                    .encodeToString(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            logger.error("Error while encrypting input text!", exception);
        }
        return null;
    }

    /**
     * Decrypt a string using a secret key.
     *
     * @param strToDecrypt String to decrypt.
     * @param secret Secret passphrase.
     * @return The decrypted string. Returns null if the passphrase was wrong.
     */
    public static String decrypt(String strToDecrypt, String secret) {
        try {
            setKey(secret);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(
                    cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)),
                    StandardCharsets.UTF_8);
        } catch (Exception exception) {
            logger.error("Error while decrypting input text!", exception);
            return null;
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
            conn.setRequestProperty("User-Agent", Main.userAgent);
            conn.connect();

            InputStream in = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            rawJSON = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            reader.close();

        } catch (IOException exception) {
            logger.error("Unable to read URL " + url, exception);
            JFrame errorWindow = new ErrorWindow(null, exception);
            errorWindow.dispose();
            return null;
        }

        if (rawJSON.isEmpty()) {
            logger.error("Read JSON from " + url + " returned an empty string!");
            return null;
        }
        return new JSONObject(rawJSON);
    }
}
