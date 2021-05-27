package lol.hyper.customlauncher;

import lol.hyper.customlauncher.accounts.JSONManager;
import lol.hyper.customlauncher.windows.MainWindow;
import lol.hyper.customlauncher.updater.Updater;
import org.json.JSONObject;

import javax.swing.*;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) {
        JFrame mainWindow = new MainWindow("Launcher");

        JSONObject optionsFile = JSONManager.readFile(JSONManager.configFile);
        String installPath = optionsFile.getString("ttrInstallLocation");

        JFrame updater = new Updater("Updater", Paths.get(installPath));
    }
}
