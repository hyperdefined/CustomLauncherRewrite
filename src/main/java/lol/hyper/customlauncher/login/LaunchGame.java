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

import lol.hyper.customlauncher.Main;
import lol.hyper.customlauncher.generic.ErrorWindow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class LaunchGame extends Thread {

    public final Logger logger = LogManager.getLogger(this);
    final String cookie;
    final String gameServer;

    public LaunchGame(String cookie, String gameServer) {
        this.cookie = cookie;
        this.gameServer = gameServer;
    }

    public void run() {
        String[] command = {"cmd", "/c", "TTREngine.exe"};
        ProcessBuilder pb = new ProcessBuilder(command);

        // dirty little trick to redirect the output
        // the game freezes if you don't do this
        // https://stackoverflow.com/a/58922302
        pb.redirectOutput(ProcessBuilder.Redirect.PIPE);
        pb.redirectErrorStream(true);
        pb.directory(Main.TTR_INSTALL_DIR);

        Map<String, String> env = pb.environment();
        env.put("TTR_GAMESERVER", this.gameServer);
        env.put("TTR_PLAYCOOKIE", this.cookie);
        try {
            Process p = pb.start();
            p.getInputStream().close();
        } catch (IOException e) {
            logger.error("Unable to launch game!", e);
            JFrame errorWindow =
                    new ErrorWindow(
                            "Unable to launch game.\n"
                                    + e.getClass().getCanonicalName()
                                    + ": "
                                    + e.getMessage());
            errorWindow.dispose();
        }
    }
}
