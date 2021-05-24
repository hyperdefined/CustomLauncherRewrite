package lol.hyper.customlauncher;

public class Account {

    private final String username;
    private final String password;
    private boolean secure = true;

    public Account(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

}
