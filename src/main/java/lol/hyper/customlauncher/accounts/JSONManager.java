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

import org.json.JSONException;
import org.json.JSONObject;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class JSONManager {

    public static final File accountsFile =
            Paths.get("config" + File.separator + "accounts.json").toFile();
    public static final File configFile =
            Paths.get("config" + File.separator + "config.json").toFile();
    private static SecretKeySpec secretKey;

    /**
     * Read data from JSON file.
     *
     * @return JSONObject with JSON data.
     */
    public static JSONObject readFile(File file) {
        JSONObject object = null;
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                line = br.readLine();
            }
            object = new JSONObject(sb.toString());
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return object;
    }

    /**
     * Write data to JSON file.
     *
     * @param jsonToWrite Data to write to file. This much be a JSON string.
     */
    private static void writeFile(String jsonToWrite, File file) {
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(jsonToWrite);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get all of the accounts from the accounts file.
     *
     * @return List of the accounts.
     */
    public static List<Account> getAccounts() {
        List<Account> accounts = new ArrayList<>();
        JSONObject accountsJSON = readFile(accountsFile);
        for (Integer x : getAccountIndexes(accountsJSON)) {
            JSONObject temp;
            try {
                temp = (JSONObject) accountsJSON.get(x.toString());
            } catch (JSONException exception) {
                continue;
            }
            String username = (String) temp.get("username");
            String password = (String) temp.get("password");
            Account account = new Account(username, password);
            accounts.add(account);
        }
        return accounts;
    }

    /**
     * This will get all of the account indexes from the accounts file. They are always not in
     * order, so we have to manually save this list.
     *
     * @param jsonObject The object to get the indexes for.
     * @return A list of the indexes.
     */
    private static ArrayList<Integer> getAccountIndexes(JSONObject jsonObject) {
        ArrayList<Integer> indexes = new ArrayList<>();
        for (String x : jsonObject.keySet()) {
            indexes.add(Integer.valueOf(x));
        }
        return indexes;
    }

    /**
     * Adds a new account to the accounts file.
     * @param username Account username.
     * @param password Account password.
     * @param secret Secret passphrase. (Used to encrypt the password.)
     */
    public static void addNewAccount(String username, char[] password, char[] secret) {
        JSONObject accountsJSON = readFile(accountsFile);
        ArrayList<Integer> indexes = getAccountIndexes(accountsJSON);
        int numberWeUse = findFirstMissing(indexes);
        if (accountsJSON.length() == 0) {
            numberWeUse = 0;
        }

        // if there is no missing number in the sequence, then get the last number and add 1
        // this will use the next number in the sequence
        if (numberWeUse == -1) {
            numberWeUse = indexes.get(indexes.size() - 1) + 1;
        }

        Map m = new LinkedHashMap(3);
        m.put("username", username);
        m.put("password", encrypt(new String(password), new String(secret)));
        accountsJSON.put(String.valueOf(numberWeUse), m);
        writeFile(accountsJSON.toString(), accountsFile);
    }

    /**
     * Delete an account from the accounts file.
     *
     * @param index Index of account to delete.
     */
    public static void deleteAccount(int index) {
        JSONObject accountsJSON = readFile(accountsFile);
        accountsJSON.remove(String.valueOf(index));
        writeFile(accountsJSON.toString(), accountsFile);
    }

    /**
     * Find the first missing number from the accounts indexes. This will check for a gap (ex: 1,2,4
     * - 3 is the "first missing"). This is mainly used so the accounts file doesn't have gaps in
     * the account indexes. This will tell the account creator to use the missing index.
     *
     * @param indexes The indexes to check.
     * @return The first missing number in the sequence.
     */
    private static int findFirstMissing(ArrayList<Integer> indexes) {
        for (int i = 0; i < indexes.size(); i++) {
            if (!indexes.contains(i)) {
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
            e.printStackTrace();
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
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            System.out.println("Error while encrypting: " + e.toString());
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
            return Arrays.toString(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
        } catch (Exception e) {
            System.out.println("Error while decrypting: " + e.toString());
            return null;
        }
    }

    /**
     * Add/remove a value from the config.
     *
     * @param key Key for the JSON.
     * @param value Value for the key.
     * @param remove Should we remove this entry or add this entry?
     */
    public static void editConfig(String key, Object value, boolean remove) {
        JSONObject config = readFile(configFile);
        if (remove) {
            config.remove(key);
        } else {
            config.put(key, value);
        }
        writeFile(config.toString(), configFile);
    }

    /**
     * Check the config to see if we should check for TTR updates.
     *
     * @return Yes/no if we should.
     */
    public static boolean shouldWeUpdate() {
        JSONObject config = readFile(configFile);
        return config.getBoolean("autoCheckTTRUpdates");
    }
}
