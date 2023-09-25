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

package lol.hyper.customlauncher.tools;

import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OSDetection {

    /**
     * Stores what OS we are running. Can be win32/win64 or linux.
     */
    public static String osType = null;

    static {
        if (SystemUtils.IS_OS_WINDOWS) {
            if (System.getProperty("sun.arch.data.model").equals("64")) {
                osType = "win64";
            }
            if (System.getProperty("sun.arch.data.model").equals("32")) {
                osType = "win32";
            }
        }
        if (SystemUtils.IS_OS_LINUX) {
            osType = "linux";
        }

        Logger logger = LogManager.getLogger(OSDetection.class);
        if (osType == null) {
            new PopUpWindow(null, "We are unable to detect your operating system. Please report this to the GitHub page.\nInclude the log file please.");
            logger.error("Unable to determine operating system! Are we running something not supported?");
            logger.info("OS: " + System.getProperty("os.name") + " " + System.getProperty("sun.arch.data.model") + "bit");
            logger.info("Arch: " + System.getProperty("os.arch"));
            logger.info("Java: " + System.getProperty("java.vm.version") + " (" + System.getProperty("java.vendor") + ")");
            System.exit(1);
        }
        logger.info("OS type detected as " + osType);
        logger.info("Actual OS: " + System.getProperty("os.name") + " " + System.getProperty("sun.arch.data.model") + "bit");
        logger.info("Arch: " + System.getProperty("os.arch"));
        logger.info("Java: " + System.getProperty("java.vm.version") + " (" + System.getProperty("java.vendor") + ")");
    }

    /**
     * Are we running Linux?
     *
     * @return Yes or no.
     */
    public static boolean isLinux() {
        return osType.equals("linux");
    }

    /**
     * Are we running Windows?
     *
     * @return Yes or no.
     */
    public static boolean isWindows() {
        return osType.contains("win");
    }
}
