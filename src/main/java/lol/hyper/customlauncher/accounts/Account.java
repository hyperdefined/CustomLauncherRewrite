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

public record Account(String username, String password, Type accountType) {

    @Override
    public String toString() {
        return this.username;
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
}
