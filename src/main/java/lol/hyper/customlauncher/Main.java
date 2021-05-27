package lol.hyper.customlauncher;

import lol.hyper.customlauncher.accounts.JSONManager;
import lol.hyper.customlauncher.accounts.windows.MainWindow;
import lol.hyper.customlauncher.updater.InvalidPath;
import lol.hyper.customlauncher.updater.Updater;
import org.json.JSONObject;

import javax.swing.*;
import java.nio.file.Paths;

public class Main {

    public static String installPath;
    public static final String DEFAULT_INSTALL = "C:\\Program Files (x86)\\Toontown Rewritten";

    public static void main(String[] args) {
        // check to see if the default ttr install is there
        // if not, use the path in the config
        if (!Paths.get(DEFAULT_INSTALL).toFile().exists()) {
            JSONObject optionsFile = JSONManager.readFile(JSONManager.configFile);
            installPath = optionsFile.getString("ttrInstallLocation");
            // check the config path
            if (!Paths.get(installPath).toFile().exists()) {
                InvalidPath invalidPath = new InvalidPath("Error");
                System.exit(1);
            }
            JFrame updater = new Updater("Updater", Paths.get(installPath));
            updater.dispose();
        } else {
            JFrame updater = new Updater("Updater", Paths.get(DEFAULT_INSTALL));
            updater.dispose();
        }
        JFrame mainWindow = new MainWindow("Launcher");
        mainWindow.dispose();
    }
}
