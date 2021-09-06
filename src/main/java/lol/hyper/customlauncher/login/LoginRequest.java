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

package lol.hyper.customlauncher.login;

import java.util.HashMap;

public class LoginRequest {

    final HashMap<String, String> requestDetails;

    public LoginRequest() {
        requestDetails = new HashMap<>();
    }

    /**
     * Login requests get stored as a HashMap. This will get the details of said login request.
     * @return The HashMap request with all details, saved as key=value.
     */
    public HashMap<String, String> getRequestDetails() {
        return requestDetails;
    }

    /**
     * Add a value to the login request.
     * @param key The key to save it as.
     * @param value The value of said key.
     */
    public void addDetails(String key, String value) {
        requestDetails.put(key, value);
    }
}
