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
    private static final String REQUEST_URL =
            "https://www.toontownrewritten.com/api/login?format=json";
    private static final String USER_AGENT =
            "CustomLauncherRewrite https://github.com/hyperdefined/CustomLauncherRewrite";

    /**
     * Handle the result of the login request. This will take a login request and act based on that
     * login request. This lets us send the login request back to this method over and over again.
     *
     * @param loginRequest The login request to process.
     */
    public static void handleLoginRequest(LoginRequest loginRequest) {
        int attempts = 0;
        HashMap<String, String> request;
        try {
            logger.info("Sending login request...");
            // send the login request to TTR
            request = sendRequest(loginRequest).getRequestDetails();
            attempts += 1;
        } catch (Exception e) {
            logger.error("Unable to send login request to TTR!", e);
            JFrame errorWindow =
                    new ErrorWindow(
                            "Unable to send login request to TTR.\n"
                                    + e.getClass().getCanonicalName()
                                    + ": "
                                    + e.getMessage());
            errorWindow.dispose();
            return;
        }

        // get the login status
        String status = request.get("success");
        String banner = request.get("banner");
        String eta = request.get("eta");

        logger.info("banner=" + banner);
        logger.info("status=" + status);

        // act based on the login status
        switch (status) {
            case "false" -> // false is invalid login details / maintenance
                    {
                        logger.info("Returned false: " + banner);
                        JFrame errorWindow = new ErrorWindow(banner);
                        errorWindow.dispose();
                    }
            case "partial" -> // partial is used for 2FA or ToonGuard
                    {
                        logger.info("Returned partial: " + banner);
                        new TwoFactorAuth("Enter Code", banner, request.get("responseToken"));
                    }
            case "true" -> // login was successful
                    {
                        logger.info("Returned true: " + banner);
                        String gameServer = request.get("gameserver");
                        String cookie = request.get("cookie");
                        LaunchGame launchGame = new LaunchGame(cookie, gameServer);
                        launchGame.start();
                    }
            case "delayed" -> // login request was put into a queue
                    {
                        logger.info("Returned delayed: " + banner);
                        if (Integer.parseInt(eta) >= 5 || attempts >= 5) {
                            JFrame infoWindow =
                                    new InfoWindow(
                                            "You were placed in a queue. Press OK to try again in 5 seconds.");
                            infoWindow.dispose();

                            // send the login request again after 5 seconds
                            try {
                                TimeUnit.SECONDS.sleep(5);
                            } catch (InterruptedException e) {
                                logger.error(e);
                            }
                            LoginRequest newLoginRequest = new LoginRequest();
                            newLoginRequest.addDetails("queueToken", request.get("queueToken"));
                            LoginHandler.handleLoginRequest(newLoginRequest);
                            attempts += 1;
                        }
                        else {
                            try {
                                //Try again every second.
                                //If we go over 5 attempts, wait 5 seconds and notify the user
                                TimeUnit.SECONDS.sleep(1);
                            } catch (InterruptedException e) {
                                logger.error(e);
                            }
                            LoginRequest newLoginRequest = new LoginRequest();
                            newLoginRequest.addDetails("queueToken", request.get("queueToken"));
                            LoginHandler.handleLoginRequest(newLoginRequest);
                            attempts += 1;
                        }
                    }
            default -> // TTR sent back a weird status that we don't know about
                    {
                        logger.error("Weird login response: " + status);
                        logger.info(request);
                        JFrame errorWindow =
                                new ErrorWindow(
                                        "TTR sent back a weird response, or we got an invalid response.\nCheck the log for more information.");
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
            urlParameters.add(new BasicNameValuePair(x, loginRequest.getRequestDetails().get(x)));
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
