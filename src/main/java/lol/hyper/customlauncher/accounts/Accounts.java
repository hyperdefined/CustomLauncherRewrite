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

import lol.hyper.customlauncher.tools.ExceptionWindow;
import lol.hyper.customlauncher.tools.JSONManager;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Accounts {

    private final List<Account> accounts = new ArrayList<>();
    public static final File ACCOUNTS_FILE = new File("config", "accounts.json");

    public final Logger logger = LogManager.getLogger(this);

    /**
     * Creates an Account object.
     */
    public Accounts() {
        // create the base accounts files
        if (!ACCOUNTS_FILE.exists()) {
            JSONArray newAccounts = new JSONArray();
            JSONManager.writeFile(newAccounts, Accounts.ACCOUNTS_FILE);
            logger.info("Creating base accounts file...");
        }
    }

    /**
     * Get an updated list of accounts
     *
     * @return The accounts.
     */
    public List<Account> getAccounts() {
        loadAccountsFromFile();
        return accounts;
    }

    /**
     * Create a new account and save it.
     *
     * @param username    The username.
     * @param password    The password.
     * @param accountType The type of account.
     */
    public void addAccount(String username, String password, Account.Type accountType) {
        Account newAccount = new Account(username, password, accountType);
        accounts.add(newAccount);
        writeAccounts();
    }

    /**
     * Loads accounts saved in the accounts.json file.
     */
    private void loadAccountsFromFile() {
        accounts.clear();
        JSONArray accountsJSON = new JSONArray(JSONManager.readFile(ACCOUNTS_FILE));
        for (int i = 0; i < accountsJSON.length(); i++) {
            JSONObject currentAccount = accountsJSON.getJSONObject(i);
            String username = currentAccount.getString("username");
            String password = currentAccount.getString("password");
            Account.Type accountType = null;

            if (currentAccount.has("version")) {
                // it has a version already set, get that version
                int version = currentAccount.getInt("version");
                for (Account.Type type : Account.Type.values()) {
                    if (type.toInt() == version) {
                        accountType = type;
                    }
                }
            }

            // account has a version attached, load that version
            if (accountType != null) {
                Account account = new Account(username, password, accountType);
                accounts.add(account);
                continue;
            }

            // old accounts before plaintext was added were encrypted by default
            if (!currentAccount.has("encrypted")) {
                accountType = Account.Type.LEGACY_ENCRYPTED;
                Account account = new Account(username, password, accountType);
                accounts.add(account);
                continue;
            }
            // has encrypted tag, see if it's encrypted or not
            if (!currentAccount.getBoolean("encrypted")) {
                // plaintext account
                accountType = Account.Type.PLAINTEXT;
            } else {
                // version 1 (legacy)
                accountType = Account.Type.LEGACY_ENCRYPTED;
            }

            Account account = new Account(username, password, accountType);
            accounts.add(account);

            // force write to update the json file
            // this will convert the old accounts system over
            writeAccounts();
        }
    }

    /**
     * Delete an account from the file.
     *
     * @param accountToRemove Account that should be deleted.
     */
    public void removeAccount(Account accountToRemove) {
        loadAccountsFromFile();
        accounts.remove(accountToRemove);
        writeAccounts();
    }

    /**
     * Save all accounts to the accounts file.
     */
    public void writeAccounts() {
        JSONArray accountsArray = new JSONArray();
        for (Account account : accounts) {
            JSONObject accountObj = new JSONObject();
            accountObj.put("username", account.username());
            accountObj.put("password", account.password());
            accountObj.put("version", account.accountType().toInt());
            accountsArray.put(accountObj);
        }
        JSONManager.writeFile(accountsArray, ACCOUNTS_FILE);
    }
}
