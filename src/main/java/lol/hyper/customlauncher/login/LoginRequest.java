package lol.hyper.customlauncher.login;
import java.util.HashMap;


public class LoginRequest {

    HashMap<String, String> requestDetails;

    public LoginRequest() {
        requestDetails = new HashMap<>();
    }

    public HashMap<String, String> getRequestDetails() {
        return requestDetails;
    }

    public void addDetails(String key, String value) {
        requestDetails.put(key, value);
    }
}
