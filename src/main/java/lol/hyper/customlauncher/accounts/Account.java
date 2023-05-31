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

public class Account {

    private final String username;
    private String password;
    private Type accountType;

    /**
     * Creates an account.
     *
     * @param username    The username to the account.
     * @param password    The password to the account. Can be plaintext or encrypted.
     * @param accountType The type of account. Make sure to pass in the correct password for the type.
     */
    public Account(String username, String password, Type accountType) {
        this.username = username;
        this.password = password;
        this.accountType = accountType;
    }

    /**
     * Set the password for this account. Can be encrypted or plaintext. Make sure to update the type.
     *
     * @param newPassword The new password.
     */
    public void setPassword(String newPassword) {
        this.password = newPassword;
    }

    /**
     * Set the account type. See {@link Type}.
     * @param accountType The new account type.
     */
    public void setAccountType(Type accountType) {
        this.accountType = accountType;
    }

    /**
     * Get the password for this account. Can be plaintext or encrypted.
     * @return The password.
     */
    public String password() {
        return password;
    }

    /**
     * Get the username for this account.
     * @return The username.
     */
    public String username() {
        return username;
    }

    /**
     * Get the account type for this account. See {@link Type}.
     * @return The account type.
     */
    public Type accountType() {
        return accountType;
    }

    /**
     * The type the account is.
     */
    public enum Type {
        /**
         * PLAINTEXT is for accounts storing their password in plaintext.
         */
        PLAINTEXT(0),
        /**
         * LEGACY_ENCRYPTED is for accounts storing their password in the old encryption. See {@link AccountEncryption#decryptLegacy(String, String)}.
         */
        LEGACY_ENCRYPTED(1),
        /**
         * ENCRYPTED is for accounts storing their password encrypted. See {@link AccountEncryption#encrypt(String, String)}.
         */
        ENCRYPTED(2);

        private final int accountType;

        Type(int accountType) {
            this.accountType = accountType;
        }

        public int toInt() {
            return accountType;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof Account account)) {
            return false;
        }

        return account.username().equalsIgnoreCase(this.username());
    }

    @Override
    public String toString() {
        return this.username;
    }
}
