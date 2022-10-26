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
import org.json.JSONArray;
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

    /**
     * Read contents of a file.
     *
     * @return Contents of a file.
     */
    public static String readFile(File file) {
        byte[] encoded = new byte[0];
        try {
            encoded = Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            Main.logger.error("Unable to read file " + file, e);
            JFrame errorWindow = new ErrorWindow(null, e);
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
        } catch (IOException e) {
            Main.logger.error("Unable to write file " + file, e);
            JFrame errorWindow = new ErrorWindow(null, e);
            errorWindow.dispose();
        }
    }

    /**
     * Get all the accounts from the accounts file.
     *
     * @return List of the accounts.
     */
    public static List<Account> getAccounts() {
        List<Account> accounts = new ArrayList<>();
        JSONArray accountsJSON = new JSONArray(readFile(accountsFile));
        for (int i = 0; i < accountsJSON.length(); i++) {
            JSONObject currentAccount = accountsJSON.getJSONObject(i);
            String username = (String) currentAccount.get("username");
            String password = (String) currentAccount.get("password");
            // if the account doesn't have this tag, it's using the old system
            // the old system encrypts them
            boolean encrypted;
            if (!currentAccount.has("encrypted")) {
                encrypted = true;
            } else {
                encrypted = currentAccount.getBoolean("encrypted");
            }
            Account account = new Account(username, password, encrypted);
            accounts.add(account);
        }
        return accounts;
    }

    /**
     * Adds a new account to the accounts file.
     *
     * @param username Account username.
     * @param password Account's password or encrypted password.
     * @param encrypted Is the account's password encrypted?
     */
    public static void addNewAccount(String username, String password, boolean encrypted) {
        JSONArray accountsJSON = new JSONArray(readFile(accountsFile));
        JSONObject newAccount = new JSONObject();
        newAccount.put("username", username);
        newAccount.put("password", password);
        newAccount.put("encrypted", encrypted);
        accountsJSON.put(newAccount);
        writeFile(accountsJSON, accountsFile);
    }

    /**
     * Delete an account from the accounts file.
     *
     * @param account Account to delete.
     */
    public static void deleteAccount(Account account) {
        JSONArray accountsJSON = new JSONArray(readFile(accountsFile));
        accountsJSON.remove(getAccountIndex(account));
        writeFile(accountsJSON, accountsFile);
    }

    public static int getAccountIndex(Account account) {
        JSONArray accountsJSON = new JSONArray(readFile(accountsFile));
        String username = account.getUsername();
        for (int i = 0; i < accountsJSON.length(); i++) {
            JSONObject currentAccount = accountsJSON.getJSONObject(i);
            String usernameTemp = currentAccount.getString("username");
            if (usernameTemp.equalsIgnoreCase(username)) {
                return i;
            }
        }
        return -1;
    }

    public static void setKey(String myKey) {
        MessageDigest sha;
        try {
            byte[] key = myKey.getBytes(StandardCharsets.UTF_8);
            sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            secretKey = new SecretKeySpec(key, "AES");
        } catch (NoSuchAlgorithmException e) {
            Main.logger.error("Error while setting key!", e);
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
        } catch (Exception e) {
            Main.logger.error("Error while encrypting input text!", e);
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
        } catch (Exception e) {
            Main.logger.error("Error while decrypting input text!", e);
            return null;
        }
    }

    /**
     * Get a JSONObject from a URL.
     *
     * @param url The URL to get JSON from.
     * @return The URL's JSON.
     */
    public static JSONObject requestJSON(String url) {
        String rawJSON;
        try {
            URLConnection conn = new URL(url).openConnection();
            conn.setRequestProperty(
                    "User-Agent",
                    "CustomLauncherRewrite https://github.com/hyperdefined/CustomLauncherRewrite");
            conn.connect();

            InputStream in = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            rawJSON = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            reader.close();

        } catch (IOException e) {
            Main.logger.error("Unable to read URL " + url, e);
            JFrame errorWindow = new ErrorWindow(null, e);
            errorWindow.dispose();
            return null;
        }

        if (rawJSON.isEmpty()) {
            Main.logger.error("Read JSON from " + url + " returned an empty string!");
            return null;
        }
        return new JSONObject(rawJSON);
    }
}
