package lol.hyper.customlauncher.login;

import lol.hyper.customlauncher.Main;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class LaunchGame extends Thread {

    String cookie;
    String gameServer;

    public LaunchGame(String cookie, String gameServer) {
        this.cookie = cookie;
        this.gameServer = gameServer;
    }

    public void run() {
        String[] command = {"cmd", "/c", "TTREngine.exe" };
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(new File(Main.installPath));

        Map<String, String> env = pb.environment();
        env.put("TTR_GAMESERVER", this.gameServer);
        env.put("TTR_PLAYCOOKIE", this.cookie);
        try {
            Process p = pb.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
