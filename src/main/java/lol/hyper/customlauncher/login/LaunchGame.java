package lol.hyper.customlauncher.login;

import lol.hyper.customlauncher.Main;

import java.io.IOException;
import java.util.Map;

public class LaunchGame {

    public static void launchGame(String cookie, String gameServer) {
        ProcessBuilder pb = new ProcessBuilder(Main.installPath + "TTREngine.exe");

        Map<String, String> env = pb.environment();
        env.put("TTR_GAMESERVER", gameServer);
        env.put("TTR_PLAYCOOKIE", cookie);
        try {
            Process p = pb.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
