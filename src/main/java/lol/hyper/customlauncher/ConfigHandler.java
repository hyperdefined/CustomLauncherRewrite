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

import lol.hyper.customlauncher.tools.ExceptionWindow;
import lol.hyper.customlauncher.tools.JSONUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

public class ConfigHandler {

    /**
     * The main config file.
     */
    public final File CONFIG_FILE = new File("config", "config.json");
    /**
     * The config version, used for detecting and changes.
     */
    public final int CONFIG_VERSION = 1;
    /**
     * TTR's install folder.
     */
    public static File INSTALL_LOCATION;
    /**
     * The config's content.
     */
    private JSONObject configJSON;
    /**
     * The ConfigHandler logger.
     */
    private final Logger logger = LogManager.getLogger(this);

    /**
     * Initializes the config.
     */
    public ConfigHandler() {
        File CONFIG_FOLDER = new File("config");
        if (!CONFIG_FOLDER.exists()) {
            try {
                logger.info("Creating config folder at " + CONFIG_FOLDER.getAbsolutePath());
                Files.createDirectory(CONFIG_FOLDER.toPath());
            } catch (IOException exception) {
                logger.error("Cannot create config folder!", exception);
                new ExceptionWindow(exception);
            }
        }
        loadConfig(true);
    }

    /**
     * Should we show invasion notifications?
     *
     * @return Yes/No
     */
    public boolean showCogInvasionNotifications() {
        return configJSON.getBoolean("showInvasionNotifications");
    }

    /**
     * Should we show field office notifications?
     *
     * @return Yes/No
     */
    public boolean showFieldOfficeNotifications() {
        return configJSON.getBoolean("showFieldOfficeNotifications");
    }

    /**
     * Edit multiple config values at once.
     *
     * @param values The key=values to change to.
     */
    public void editMultiple(Map<String, Object> values) {
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (configJSON.has(key)) {
                configJSON.put(key, value);
            }
        }
        JSONUtils.writeFile(configJSON, CONFIG_FILE);
    }

    /**
     * Sets the default config values. If anything is missing, it will update the config.
     */
    private void setDefaults() {
        boolean changed = false;
        if (!configJSON.has("showInvasionNotifications")) {
            configJSON.put("showInvasionNotifications", true);
            changed = true;
        }
        if (!configJSON.has("showFieldOfficeNotifications")) {
            configJSON.put("showFieldOfficeNotifications", true);
            changed = true;
        }
        if (!configJSON.has("ttrInstallLocation")) {
            configJSON.put("ttrInstallLocation", System.getProperty("user.dir") + File.separator + "ttr-files");
            changed = true;
        }
        if (changed) {
            configJSON.put("version", CONFIG_VERSION);
            JSONUtils.writeFile(configJSON, CONFIG_FILE);
        }
    }

    /**
     * Read the config.json file.
     *
     * @param log Should we log the contents?
     */
    public void loadConfig(boolean log) {
        if (!CONFIG_FILE.exists()) {
            configJSON = new JSONObject();
        } else {
            configJSON = new JSONObject(JSONUtils.readFile(CONFIG_FILE));
            if (!configJSON.has("version")) {
                logger.info("Config does not have a version set, adding to it");
                configJSON.put("version", CONFIG_VERSION);
                JSONUtils.writeFile(configJSON, CONFIG_FILE);
            }
            if (configJSON.getInt("version") != CONFIG_VERSION) {
                logger.warn("Config version is not correct! Somethings will not work correctly. Version should be " + CONFIG_VERSION + " but read " + configJSON.getInt("version"));
            }
        }
        setDefaults();
        INSTALL_LOCATION = new File(configJSON.getString("ttrInstallLocation"));

        // create the ttr-files folder
        if (!INSTALL_LOCATION.exists()) {
            try {
                Files.createDirectory(INSTALL_LOCATION.toPath());
                logger.info("Creating TTR install folder at " + INSTALL_LOCATION.getAbsolutePath());
                new FirstLaunch();
            } catch (IOException exception) {
                logger.error("Cannot create TTR folder!", exception);
                new ExceptionWindow(exception);
            }
        }

        if (log) {
            logger.info("Config version: " + configJSON.getInt("version"));
            logger.info("showInvasionNotifications: " + showCogInvasionNotifications());
            logger.info("showFieldOfficeNotifications: " + showFieldOfficeNotifications());
            logger.info("ttrInstallLocation: " + INSTALL_LOCATION.getAbsolutePath());
        }
    }
}
