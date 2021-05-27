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

package lol.hyper.customlauncher.login.windows;

import lol.hyper.customlauncher.login.LoginHandler;
import lol.hyper.customlauncher.login.LoginRequest;

import javax.swing.*;
import java.util.concurrent.TimeUnit;

public class QueueLogin extends JFrame {

    public QueueLogin(String title, String queueToken) {
        JFrame frame = new JFrame(title);
        frame.setSize(370, 150);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setResizable(false);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        JOptionPane.showMessageDialog(frame, "You were placed in a queue. Press OK to try again in 5 seconds.", title, JOptionPane.INFORMATION_MESSAGE);
        frame.dispose();
        frame.setLocationRelativeTo(null);
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.addDetails("queueToken", queueToken);
        LoginHandler.handleLoginRequest(loginRequest);
    }
}