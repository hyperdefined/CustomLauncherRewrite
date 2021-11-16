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
    private final String password;

    /**
     * Create a new account object.
     *
     * @param username Username of account.
     * @param password Password of account. Must be encrypted.
     */
    public Account(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Returns the username of an account.
     *
     * @return Username of account.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns the password of an account. This password is encrypted.
     *
     * @return Password of account.
     */
    public String getPassword() {
        return password;
    }
}
