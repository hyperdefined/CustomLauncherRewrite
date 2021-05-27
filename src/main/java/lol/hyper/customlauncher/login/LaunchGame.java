package lol.hyper.customlauncher.login;

import lol.hyper.customlauncher.Main;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class LaunchGame extends Thread {

    final String cookie;
    final String gameServer;

    public LaunchGame(String cookie, String gameServer) {
        this.cookie = cookie;
        this.gameServer = gameServer;
    }

    public void run() {
        String[] command = {"cmd", "/c", "TTREngine.exe" };
        ProcessBuilder pb = new ProcessBuilder(command);

        // dirty little trick to redirect the output
        // the game freezes if you don't do this
        // https://stackoverflow.com/a/58922302
        pb.redirectOutput(ProcessBuilder.Redirect.PIPE);
        pb.redirectErrorStream(true);
        pb.directory(new File(Main.pathToUse));

        Map<String, String> env = pb.environment();
        env.put("TTR_GAMESERVER", this.gameServer);
        env.put("TTR_PLAYCOOKIE", this.cookie);
        try {
            Process p = pb.start();
            p.getInputStream().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
