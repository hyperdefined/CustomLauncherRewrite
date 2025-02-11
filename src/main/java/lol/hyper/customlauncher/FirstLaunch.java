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
import lol.hyper.customlauncher.tools.OSDetection;
import lol.hyper.customlauncher.tools.PopUpWindow;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

public class FirstLaunch {
    /**
     * The FirstLaunch logger.
     */
    private final Logger logger = LogManager.getLogger(this);

    /**
     * Runs the process for first launch.
     */
    public FirstLaunch() {
        new PopUpWindow(null, "Welcome to CustomLauncherRewrite! I am first going to detect for an existing TTR install.\n I will copy screenshots, settings, and resource packs.");
        if (OSDetection.isLinux()) {
            copyLinuxInstall();
        } else if (OSDetection.isWindows()) {
            copyWindowsInstall();
        }
    }

    /**
     * Locates and copies various files from another TTR installation on Windows.
     */
    private void copyWindowsInstall() {
        File windowsInstall = null;
        File[] roots = File.listRoots();
        boolean foundInstall = false;
        for (File root : roots) {
            String WINDOWS_INSTALL = "Program Files (x86)\\Toontown Rewritten";
            windowsInstall = new File(root, WINDOWS_INSTALL);
            if (windowsInstall.exists()) {
                foundInstall = true;
                break;
            }
        }
        // can't find install folder
        if (!foundInstall) {
            new PopUpWindow(null, "I am unable to find your TTR install location. You'll have to manually copy things over that you wish to keep.");
            return;
        }
        logger.info("Found existing Windows TTR install at {}", windowsInstall);
        copyFiles(windowsInstall);
    }

    /**
     * Locates and copies various files from another TTR installation on Linux.
     */
    private void copyLinuxInstall() {
        String LINUX_INSTALL = "/.var/app/com.toontownrewritten.Launcher/data/";
        File linuxInstall = new File(System.getProperty("user.home") + LINUX_INSTALL);
        if (!linuxInstall.exists()) {
            new PopUpWindow(null, "I am unable to find your TTR install location. You'll have to manually copy things over that you wish to keep.");
            return;
        }
        logger.info("Found existing Linux TTR install at {}", linuxInstall);
        copyFiles(linuxInstall);
    }

    /**
     * Copies the files from a given source folder into the "ttr-files" folder.
     *
     * @param source The source folder.
     */
    private void copyFiles(File source) {
        // we found the installation, copy files over
        File settings = new File(source, "settings.json");
        File resourcePacks = new File(source, "resources");
        File screenshots = new File(source, "screenshots");
        File newInstall = new File("ttr-files");
        try {
            if (settings.exists()) {
                logger.info("Copying {} --> {}{}settings.json", settings.getAbsolutePath(), newInstall.getAbsolutePath(), File.separator);
                FileUtils.copyFileToDirectory(settings, newInstall);
            }
            if (resourcePacks.exists()) {
                logger.info("Copying {} --> {}{}resources", resourcePacks.getAbsolutePath(), newInstall.getAbsolutePath(), File.separator);
                FileUtils.copyDirectory(resourcePacks, new File(newInstall, "resources"));
            }
            if (screenshots.exists()) {
                logger.info("Copying {} --> {}{}screenshots", screenshots.getAbsolutePath(), newInstall.getAbsolutePath(), File.separator);
                FileUtils.copyDirectory(screenshots, new File(newInstall, "screenshots"));
            }
        } catch (IOException exception) {
            logger.error("Unable to copy TTR files!", exception);
            new ExceptionWindow(exception);
        }
    }
}
