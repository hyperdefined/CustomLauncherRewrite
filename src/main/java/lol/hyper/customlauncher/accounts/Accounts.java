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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

public class Accounts {

    private final ArrayList<Account> accounts = new ArrayList<>();
    private final File ACCOUNTS_FILE = new File("config", "accounts.json");

    public Accounts() {
        loadAccountsFromFile();
    }

    /**
     * Get an updated list of accounts
     *
     * @return The accounts.
     */
    public ArrayList<Account> getAccounts() {
        loadAccountsFromFile();
        return accounts;
    }

    /**
     * Create a new account and save it.
     *
     * @param username The username.
     * @param password The password.
     * @param encrypted If the account password is encrypted.
     */
    public void addAccount(String username, String password, boolean encrypted) {
        Account newAccount = new Account(username, password, encrypted);
        accounts.add(newAccount);
        writeAccounts();
    }

    /** Loads accounts saved in the accounts.json file. */
    private void loadAccountsFromFile() {
        accounts.clear();
        JSONArray accountsJSON = new JSONArray(JSONManager.readFile(ACCOUNTS_FILE));
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

    /** Save all accounts to the accounts file. */
    private void writeAccounts() {
        JSONArray accountsArray = new JSONArray();
        for (Account account : accounts) {
            JSONObject accountObj = new JSONObject();
            accountObj.put("username", account.username());
            accountObj.put("password", account.password());
            accountObj.put("encrypted", account.encrypted());
            accountsArray.put(accountObj);
        }
        JSONManager.writeFile(accountsArray, ACCOUNTS_FILE);
    }
}
