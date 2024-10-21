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

import lol.hyper.customlauncher.ConfigHandler;
import lol.hyper.customlauncher.tools.ExceptionWindow;
import lol.hyper.customlauncher.tools.OSDetection;
import lol.hyper.customlauncher.tools.PopUpWindow;
import lol.hyper.customlauncher.ttrupdater.TTRUpdater;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public final class LaunchGame extends Thread {

    /**
     * The LaunchGame logger.
     */
    public final Logger logger = LogManager.getLogger(this);
    /**
     * The account's login cookie.
     */
    private final String cookie;
    /**
     * The game server to use.
     */
    private final String gameServer;
    /**
     * The file to check for TTR updates.
     */
    private final String manifest;

    /**
     * Creates a LaunchGame instance.
     *
     * @param cookie     The login cookie to use.
     * @param gameServer The game server to use.
     */
    public LaunchGame(String cookie, String gameServer, String manifest) {
        this.cookie = cookie;
        this.gameServer = gameServer;
        this.manifest = manifest;
    }

    /**
     * Launch the game.
     */
    public void run() {
        ConfigHandler configHandler = new ConfigHandler();
        File installPath = configHandler.getInstallPath();
        if (!installPath.exists()) {
            JOptionPane.showMessageDialog(null, "Unable to launch the game. The install location cannot be found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        ProcessBuilder pb = new ProcessBuilder();

        String[] launchCommand = null;

        // Check for TTR updates after login
        // We do this after login since TTR sends back a patch manifest to check game files after login
        // This used to not be the case, but we follow what the real launcher does
        TTRUpdater ttrUpdater = new TTRUpdater();
        ttrUpdater.setVisible(true);
        ttrUpdater.checkUpdates(manifest);

        // If the updater failed, don't launch the game
        if (!ttrUpdater.status()) {
            PopUpWindow popUpWindow = new PopUpWindow(null, "There was an issue in updating the game.");
            popUpWindow.dispose();
            return;
        }

        switch (OSDetection.osType) {
            case "linux" -> {
                launchCommand = new String[]{"./TTREngine"};
                // Make sure it's executable before running
                File fullPath = new File(installPath, "TTREngine");
                if (!fullPath.canExecute()) {
                    logger.info(fullPath.getAbsolutePath() + " is not executable. Attempting to set it.");
                    boolean result;
                    try {
                        result = fullPath.setExecutable(true);
                    } catch (SecurityException exception) {
                        logger.error("Unable to set " + fullPath.getAbsolutePath() + " as an executable!", exception);
                        new ExceptionWindow(exception);
                        return;
                    }

                    if (!result) {
                        logger.error("Unable to set " + fullPath.getAbsolutePath() + " as an executable!");
                        new PopUpWindow(null, "Unable to set " + fullPath.getAbsolutePath() + " as an executable!\nMake sure this file is executable!");
                        return;
                    } else {
                        logger.info(fullPath.getAbsolutePath() + " was set executable successfully!");
                    }
                }
            }
            case "win32" -> launchCommand = new String[]{"cmd", "/c", "TTREngine.exe"};
            case "win64" -> launchCommand = new String[]{"cmd", "/c", "TTREngine64.exe"};
        }

        if (launchCommand == null) {
            logger.error("Unable to determine operating system!");
            new PopUpWindow(null, "Unable to determine operating system!");
            return;
        }

        logger.info("Launching game from " + installPath.getAbsolutePath());

        // dirty little trick to redirect the output
        // the game freezes if you don't do this
        // https://stackoverflow.com/a/58922302
        pb.redirectOutput(ProcessBuilder.Redirect.PIPE);
        pb.redirectErrorStream(true);

        // make sure we set the working directory and command
        pb.directory(installPath);
        pb.command(launchCommand);

        Map<String, String> env = pb.environment();
        env.put("TTR_GAMESERVER", this.gameServer);
        env.put("TTR_PLAYCOOKIE", this.cookie);

        Thread t1 = new Thread(() -> {
            try {
                Process process = pb.start();
                process.getInputStream().close();
                process.waitFor();
            } catch (IOException | InterruptedException exception) {
                logger.error("Unable to launch game!", exception);
                new ExceptionWindow(exception);
            }
        });
        t1.start();
    }
}
