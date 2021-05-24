package lol.hyper.customlauncher;

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

    static final File jsonFile = Paths.get("config" + File.separator + "accounts.json").toFile();
    private static SecretKeySpec secretKey;

    /**
     * Read data from JSON file.
     * @param file File to read data from.
     * @return JSONObject with JSON data.
     */
    private static JSONObject readFile(File file) {
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
        } catch(Exception e) {
            e.printStackTrace();
        }
        return object;
    }

    /**
     * Write data to JSON file.
     * @param file File to write data to.
     * @param jsonToWrite Data to write to file. This much be a JSON string.
     */
    private static void writeFile(File file, String jsonToWrite) {
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(jsonToWrite);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Account> getAccounts() {
        List<Account> accounts = new ArrayList<>();
        JSONObject accountsFile = readFile(jsonFile);
        System.out.println(accountsFile.toString());
        for (Integer x : getAccountIndexes(accountsFile)) {
            JSONObject temp;
            try {
                temp = (JSONObject) accountsFile.get(x.toString());
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

    private static ArrayList<Integer> getAccountIndexes(JSONObject jsonObject) {
        ArrayList<Integer> indexes = new ArrayList<>();
        for (String x : jsonObject.keySet()) {
            indexes.add(Integer.valueOf(x));
        }
        return indexes;
    }

    public static void addNewAccount(String username, char[] password, char[] secret) {
        JSONObject accountsFile = readFile(jsonFile);
        ArrayList<Integer> indexes = getAccountIndexes(accountsFile);
        int numberWeUse = findFirstMissing(indexes);
        if (accountsFile.length() == 0) {
            numberWeUse = 0;
        }

        // if there is no missing number in the sequence, then get the last number and add 1
        // this will use the next number in the sequence
        if (numberWeUse == -1) {
            numberWeUse = indexes.get(indexes.size() -1) + 1;
        }

        Map m = new LinkedHashMap(3);
        m.put("username", username);
        m.put("password", encrypt(new String(password), new String(secret)));
        accountsFile.put(String.valueOf(numberWeUse), m);
        writeFile(jsonFile, accountsFile.toString());
    }

    public static void deleteAccount(int index) {
        JSONObject accountsFile = readFile(jsonFile);
        accountsFile.remove(String.valueOf(index));
        writeFile(jsonFile, accountsFile.toString());
    }

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
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static String encrypt(String strToEncrypt, String secret) {
        try {
            setKey(secret);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8)));
        }
        catch (Exception e) {
            System.out.println("Error while encrypting: " + e.toString());
        }
        return null;
    }

    public static String decrypt(String strToDecrypt, String secret) {
        try {
            setKey(secret);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
        }
        catch (Exception e) {
            System.out.println("Error while decrypting: " + e.toString());
            return null;
        }
    }
}
