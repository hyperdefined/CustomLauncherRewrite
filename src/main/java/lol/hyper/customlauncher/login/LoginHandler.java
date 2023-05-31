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

import lol.hyper.customlauncher.CustomLauncherRewrite;
import lol.hyper.customlauncher.tools.ExceptionWindow;
import lol.hyper.customlauncher.tools.PopUpWindow;
import lol.hyper.customlauncher.login.windows.TwoFactorAuth;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import javax.swing.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class LoginHandler {

    public static final Logger logger = LogManager.getLogger(LoginHandler.class);
    private static final String REQUEST_URL =
            "https://www.toontownrewritten.com/api/login?format=json";
    int attempts = 0;

    public LoginHandler(Map<String, String> loginRequest) {
        handleLoginRequest(loginRequest);
    }

    /**
     * Handle a login request. This will act based on whatever was received in the request.
     *
     * @param loginToProcess The request response.
     */
    private void handleLoginRequest(Map<String, String> loginToProcess) {
        Map<String, String> receivedRequest;
        try {
            logger.info("Sending login request...");
            // send the login request to TTR
            receivedRequest = sendRequest(loginToProcess);
            attempts += 1;
        } catch (Exception exception) {
            logger.error("Unable to send login request to TTR!", exception);
            new ExceptionWindow(exception);
            return;
        }

        // if the login failed, don't continue
        // sendRequest() will display & log errors for us
        if (receivedRequest.isEmpty()) {
            return;
        }

        logger.info("Attempt: " + attempts);
        logger.info("Received login response:");
        // get the login status
        String status = receivedRequest.get("success");
        String banner = receivedRequest.get("banner");
        String eta = receivedRequest.get("eta");

        // log the request details
        logger.info("banner=" + banner);
        logger.info("status=" + status);
        logger.info("eta=" + eta);

        // act based on the login status
        // TTR has different statuses for login responses
        switch (status) {
            case "false" -> // false is invalid login details / maintenance
                    new PopUpWindow(null, banner);
            case "partial" -> // partial is used for 2FA or ToonGuard
                    SwingUtilities.invokeLater(() -> {
                        TwoFactorAuth twoFactorAuth = new TwoFactorAuth(banner, receivedRequest.get("responseToken"));
                        twoFactorAuth.setVisible(true);
                    });
            case "true" -> // login was successful
            {
                logger.info("Login was successful, launching game...");
                String gameServer = receivedRequest.get("gameserver");
                String cookie = receivedRequest.get("cookie");
                LaunchGame launchGame = new LaunchGame(cookie, gameServer);
                launchGame.start();
            }
            case "delayed" -> // login request was put into a queue
            {
                // if the queue is over 5, tell the user
                // the queue is almost always 0
                // TTR saves your request to queueToken, so just send that back
                // to get an updated response
                if (Integer.parseInt(eta) >= 5) {
                    new PopUpWindow(null, "You were placed in a queue. Press OK to try again in 5 seconds.");

                    // send the login request again after 5 seconds
                    try {
                        TimeUnit.SECONDS.sleep(5);
                    } catch (InterruptedException e) {
                        logger.error(e);
                    }
                }
                // send the request with the queueToken
                HashMap<String, String> newLoginRequest = new HashMap<>();
                newLoginRequest.put("queueToken", receivedRequest.get("queueToken"));
                handleLoginRequest(newLoginRequest);
            }
            default -> // TTR sent back a weird status that we don't know about
            {
                logger.error("Weird login response: " + status);
                logger.info(receivedRequest);
                new PopUpWindow(null, "TTR sent back a weird response, or we got an invalid response.\nCheck the log for more information.");
            }
        }
    }

    /**
     * Send the login request to TTR.
     *
     * @param loginRequest The login request to process.
     * @return The login request that is sent back.
     */
    private Map<String, String> sendRequest(Map<String, String> loginRequest) {
        HttpPost post = new HttpPost(REQUEST_URL);
        post.setHeader("User-Agent", CustomLauncherRewrite.userAgent);
        post.setHeader("Content-type", "application/x-www-form-urlencoded");

        List<NameValuePair> urlParameters = new ArrayList<>();
        for (Map.Entry<String, String> entry : loginRequest.entrySet()) {
            urlParameters.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }

        post.setEntity(new UrlEncodedFormEntity(urlParameters));

        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response;
        try {
            response = httpClient.execute(post);
        } catch (IOException exception) {
            logger.error("Unable to send login request!", exception);
            new ExceptionWindow(exception);
            return Collections.emptyMap();
        }

        String responseData;
        try {
            responseData = EntityUtils.toString(response.getEntity());
        } catch (IOException | ParseException exception) {
            logger.error("Unable to send login request!", exception);
            new ExceptionWindow(exception);
            return Collections.emptyMap();
        }
        JSONObject responseJSON = new JSONObject(responseData);
        HashMap<String, String> receivedDetails = new HashMap<>();

        for (String x : responseJSON.keySet()) {
            receivedDetails.put(x, responseJSON.getString(x));
        }

        try {
            httpClient.close();
            response.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return receivedDetails;
    }
}
