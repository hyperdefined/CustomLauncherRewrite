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

import lol.hyper.customlauncher.generic.ErrorWindow;
import lol.hyper.customlauncher.generic.InfoWindow;
import lol.hyper.customlauncher.login.windows.TwoFactorAuth;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class LoginHandler {

    public static final Logger logger = LogManager.getLogger(LoginHandler.class);
    private static final String REQUEST_URL = "https://www.toontownrewritten.com/api/login?format=json";
    private static final String USER_AGENT =
            "CustomLauncherRewrite https://github.com/hyperdefined/CustomLauncherRewrite";

    /**
     * Handle the result of the login request. This will take a login request and act based on that
     * login request. This lets us send the login request back to this method over and over again.
     *
     * @param loginRequest The login request to process.
     */
    public static void handleLoginRequest(LoginRequest loginRequest) {
        HashMap<String, String> request;
        try {
            logger.info("Sending login request...");
            request = sendRequest(loginRequest).getRequestDetails();
        } catch (Exception e) {
            logger.error(e);
            return;
        }

        String status = request.get("success");
        String banner = request.get("banner");

        logger.info("banner=" + banner);
        logger.info("status=" + status);

        switch (status) {
            case "false": {
                // handle incorrect login
                if (banner.contains("Incorrect username")) {
                    logger.info("Username or password is wrong.");
                    JFrame errorWindow = new ErrorWindow("Login details are incorrect.");
                    errorWindow.dispose();
                }
                break;
            }
            case "partial": {
                // handle 2fa
                logger.info("Asking user for two-factor auth.");
                JFrame twoFactorAuth = new TwoFactorAuth("Enter Code", banner, request.get("responseToken"));
                break;
            }
            case "true": {
                logger.info("Login successful, launching game.");
                String gameServer = request.get("gameserver");
                String cookie = request.get("cookie");
                LaunchGame launchGame = new LaunchGame(cookie, gameServer);
                launchGame.start();
                break;
            }
            case "delayed": {
                // handle queue
                logger.info("Stuck in queue.");
                JFrame infoWindow = new InfoWindow("You were placed in a queue. Press OK to try again in 5 seconds.");
                infoWindow.dispose();
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                    logger.error(e);
                }
                LoginRequest newLoginRequest = new LoginRequest();
                newLoginRequest.addDetails("queueToken", request.get("queueToken"));
                LoginHandler.handleLoginRequest(newLoginRequest);
                break;
            }
            default: {
                logger.error("Weird login response: " + status);
                logger.info(request);
                JFrame errorWindow = new ErrorWindow(
                        "TTR sent back a weird response, or we got an invalid response.\nCheck the log for more info.");
                errorWindow.dispose();
            }
        }
    }

    /**
     * Send the login request to TTR.
     *
     * @param loginRequest The login request to process.
     * @return The login request that is sent back.
     * @throws Exception Throws any errors about reading/sending data.
     */
    private static LoginRequest sendRequest(LoginRequest loginRequest) throws Exception {
        HttpPost post = new HttpPost(REQUEST_URL);
        post.setHeader("User-Agent", USER_AGENT);
        post.setHeader("Content-type", "application/x-www-form-urlencoded");

        List<NameValuePair> urlParameters = new ArrayList<>();
        for (String x : loginRequest.getRequestDetails().keySet()) {
            urlParameters.add(
                    new BasicNameValuePair(x, loginRequest.getRequestDetails().get(x)));
        }

        post.setEntity(new UrlEncodedFormEntity(urlParameters));

        String responseData;

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
                CloseableHttpResponse response = httpClient.execute(post)) {

            responseData = EntityUtils.toString(response.getEntity());
        }
        JSONObject responseJSON = new JSONObject(responseData);
        LoginRequest newLogin = new LoginRequest();

        for (String x : responseJSON.keySet()) {
            newLogin.addDetails(x, responseJSON.getString(x));
        }

        return newLogin;
    }
}
