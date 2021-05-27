package lol.hyper.customlauncher.login;

import lol.hyper.customlauncher.login.windows.IncorrectLogin;
import lol.hyper.customlauncher.login.windows.QueueLogin;
import lol.hyper.customlauncher.login.windows.TwoFactorAuth;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LoginHandler {

    private static final String REQUEST_URL = "https://www.toontownrewritten.com/api/login?format=json";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.212 Safari/537.36";

    /**
     * Handle the result of the login request.
     * This will take a login request and act based on that login request.
     * This let's us send the login request back to this method over and over again.
     * @param loginRequest The login request to process.
     */
    public static void handleLoginRequest(LoginRequest loginRequest) {
        HashMap<String, String> request;
        try {
            request = sendRequest(loginRequest).getRequestDetails();
        } catch (Exception e) {
            return;
        }

        String status = request.get("success");
        String banner = request.get("banner");

        switch (status) {
            case "false": {
                // handle incorrect login
                if (banner.contains("Incorrect username")) {
                    JFrame incorrectLogin = new IncorrectLogin("Error");
                    incorrectLogin.dispose();
                }
                break;
            }
            case "partial": {
                // handle 2fa
                JFrame twoFactorAuth = new TwoFactorAuth("Enter Code", banner, request.get("responseToken"));
                break;
            }
            case "true": {
                String gameServer = request.get("gameserver");
                String cookie = request.get("cookie");
                LaunchGame launchGame = new LaunchGame(cookie, gameServer);
                launchGame.start();
                break;
            }
            case "delayed": {
                // handle queue
                JFrame queueLogin = new QueueLogin("Queue", request.get("queueToken"));
                queueLogin.dispose();
                break;
            }
        }
    }

    /**
     * Send the login request to TTR.
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
        JSONObject responseJSON =  new JSONObject(responseData);
        LoginRequest newLogin = new LoginRequest();

        for (String x : responseJSON.keySet()) {
            newLogin.addDetails(x, responseJSON.getString(x));
        }

        return newLogin;
    }
}