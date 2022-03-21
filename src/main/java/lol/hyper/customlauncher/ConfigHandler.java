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

package lol.hyper.customlauncher;

import lol.hyper.customlauncher.accounts.JSONManager;
import org.json.JSONObject;

import java.io.File;

public class ConfigHandler {

    public final File CONFIG_FILE = new File("config", "config.json");
    public final int CONFIG_VERSION = 1;
    public static File INSTALL_LOCATION;
    public static String installLocation;
    private JSONObject jsonObject;

    public ConfigHandler() {
        if (!CONFIG_FILE.exists()) {
            jsonObject = new JSONObject();
            jsonObject.put("showInvasionNotifications", true);
            jsonObject.put("showFieldOfficeNotifications", true);
            jsonObject.put("ttrInstallLocation", System.getProperty("user.dir") + File.separator + "ttr-files");
            jsonObject.put("version", CONFIG_VERSION);
        } else {
            jsonObject = new JSONObject(JSONManager.readFile(CONFIG_FILE));
            if (jsonObject.getInt("version") != CONFIG_VERSION) {
                Main.logger.warn("Config version is not correct! Somethings will not work correctly. Version should be " + CONFIG_VERSION + " but read " + jsonObject.getInt("version"));
            }
        }
        installLocation = jsonObject.getString("ttrInstallLocation");
        INSTALL_LOCATION = new File(installLocation);
        Main.logger.info("Config version: " + jsonObject.getInt("version"));
        Main.logger.info(jsonObject.toString());
    }

    public boolean showCogInvasionNotifications() {
        return jsonObject.getBoolean("showInvasionNotifications");
    }

    public boolean showFieldOfficeNotifications() {
        return jsonObject.getBoolean("showFieldOfficeNotifications");
    }

    public JSONObject getJsonObject() {
        return jsonObject;
    }
}
