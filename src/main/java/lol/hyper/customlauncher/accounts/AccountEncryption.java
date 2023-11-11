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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;

public class AccountEncryption {

    /**
     * The AccountEncryption logger.
     */
    private static final Logger logger = LogManager.getLogger(AccountEncryption.class);

    /**
     * Encrypts a password.
     *
     * @param password The password to encrypt.
     * @param secret   The secret passphrase from user.
     * @return Encrypted password.
     */
    public static String encrypt(String password, String secret) {
        try {
            // Generate a random 128-bit salt
            byte[] salt = new byte[16];
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.nextBytes(salt);

            // Derive a 256-bit secret key from the provided secret
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(secret.toCharArray(), salt, 65536, 256);
            SecretKey secretKey = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");

            // Encrypt the password using AES-GCM
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            byte[] iv = new byte[12]; // GCM IV length is 12 bytes
            secureRandom.nextBytes(iv);
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmParameterSpec);
            byte[] encrypted = cipher.doFinal(password.getBytes(StandardCharsets.UTF_8));

            // Combine salt, IV and encrypted data into a single byte array
            ByteBuffer byteBuffer = ByteBuffer.allocate(4 + salt.length + iv.length + encrypted.length);
            byteBuffer.putInt(salt.length);
            byteBuffer.put(salt);
            byteBuffer.put(iv);
            byteBuffer.put(encrypted);
            byte[] combined = byteBuffer.array();

            // Base64 encode the combined byte array and return as string
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception exception) {
            logger.error("Error while encrypting input text!", exception);
        }
        return null;
    }

    /**
     * Decrypts a password.
     *
     * @param encryptedData The password to decrypt.
     * @param secret        The secret passphrase from user.
     * @return Plaintext password.
     */
    public static String decrypt(String encryptedData, String secret) {
        try {
            // Decode the Base64 encoded input
            byte[] combined = Base64.getDecoder().decode(encryptedData);

            // Extract the salt, IV, and encrypted data from the combined input
            ByteBuffer byteBuffer = ByteBuffer.wrap(combined);
            int saltLength = byteBuffer.getInt();
            byte[] salt = new byte[saltLength];
            byteBuffer.get(salt);
            byte[] iv = new byte[12];
            byteBuffer.get(iv);
            byte[] encrypted = new byte[byteBuffer.remaining()];
            byteBuffer.get(encrypted);

            // Derive the secret key from the provided secret and salt
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(secret.toCharArray(), salt, 65536, 256);
            SecretKey secretKey = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");

            // Decrypt the encrypted data using AES-GCM
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec);
            byte[] decrypted = cipher.doFinal(encrypted);

            // Return the decrypted data as a string
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception exception) {
            logger.warn("Incorrect passphrase entered for account");
        }
        return null;
    }

    /**
     * Decrypts a password using the legacy method.
     *
     * @param encryptedPassword The password to decrypt.
     * @param secret            The secret passphrase from user.
     * @return Plaintext password.
     */
    public static String decryptLegacy(String encryptedPassword, String secret) {
        SecretKeySpec secretKey;
        MessageDigest sha;
        try {
            byte[] key = secret.getBytes(StandardCharsets.UTF_8);
            sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            secretKey = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(encryptedPassword)), StandardCharsets.UTF_8);
        } catch (Exception exception) {
            logger.warn("Incorrect passphrase entered for account");
            return null;
        }
    }
}
