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

import lol.hyper.customlauncher.tools.JSONManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.File;

public class ConfigHandler {

    public final File CONFIG_FILE = new File("config", "config.json");
    public final int CONFIG_VERSION = 1;
    public static File INSTALL_LOCATION;
    private JSONObject jsonObject;
    private final Logger logger = LogManager.getLogger(this);

    public ConfigHandler() {
        loadConfig(true);
    }

    public boolean showCogInvasionNotifications() {
        return jsonObject.getBoolean("showInvasionNotifications");
    }

    public boolean showFieldOfficeNotifications() {
        return jsonObject.getBoolean("showFieldOfficeNotifications");
    }

    public void editConfig(String key, Object value) {
        if (jsonObject.has(key)) {
            jsonObject.put(key, value);
            JSONManager.writeFile(jsonObject, CONFIG_FILE);
        }
    }

    /** Sets the default config values. If anything is missing it will update the config. */
    private void setDefaults() {
        boolean changed = false;
        if (!jsonObject.has("showInvasionNotifications")) {
            jsonObject.put("showInvasionNotifications", true);
            changed = true;
        }
        if (!jsonObject.has("showFieldOfficeNotifications")) {
            jsonObject.put("showFieldOfficeNotifications", true);
            changed = true;
        }
        if (!jsonObject.has("ttrInstallLocation")) {
            jsonObject.put(
                    "ttrInstallLocation",
                    System.getProperty("user.dir") + File.separator + "ttr-files");
            changed = true;
        }
        if (changed) {
            jsonObject.put("version", CONFIG_VERSION);
        }
        JSONManager.writeFile(jsonObject, CONFIG_FILE);
    }

    /** Load the config from disk into the JSON object. */
    public void loadConfig(boolean log) {
        if (!CONFIG_FILE.exists()) {
            jsonObject = new JSONObject();
        } else {
            jsonObject = new JSONObject(JSONManager.readFile(CONFIG_FILE));
            if (!jsonObject.has("version")) {
                jsonObject.put("version", CONFIG_VERSION);
                JSONManager.writeFile(jsonObject, CONFIG_FILE);
            }
            if (jsonObject.getInt("version") != CONFIG_VERSION) {
                logger.warn(
                        "Config version is not correct! Somethings will not work correctly. Version should be "
                                + CONFIG_VERSION
                                + " but read "
                                + jsonObject.getInt("version"));
            }
        }
        setDefaults();
        INSTALL_LOCATION = new File(jsonObject.getString("ttrInstallLocation"));

        if (log) {
            logger.info("Config version: " + jsonObject.getInt("version"));
            logger.info("showInvasionNotifications: " + showCogInvasionNotifications());
            logger.info("showFieldOfficeNotifications: " + showFieldOfficeNotifications());
            logger.info("ttrInstallLocation: " + INSTALL_LOCATION.getAbsolutePath());
        }
    }
}
