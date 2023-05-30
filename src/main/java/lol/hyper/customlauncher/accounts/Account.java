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

    public Account(String username, String password, Type accountType) {
        this.username = username;
        this.password = password;
        this.accountType = accountType;
    }

    public void setPassword(String newPassword) {
        this.password = newPassword;
    }

    public void setAccountType(Type accountType) {
        this.accountType = accountType;
    }

    public String password() {
        return password;
    }

    public String username() {
        return username;
    }

    public Type accountType() {
        return accountType;
    }

    public enum Type {
        PLAINTEXT(0),
        LEGACY_ENCRYPTED(1),
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
