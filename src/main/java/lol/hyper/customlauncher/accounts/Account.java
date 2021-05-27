package lol.hyper.customlauncher.accounts;

public class Account {

    private final String username;
    private final String password;

    /**
     * Create a new account object.
     * @param username Username of account.
     * @param password Password of account. Must be encrypted.
     */
    public Account(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Returns the username of an account.
     * @return Username of account.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns the password of an account. This password is encrypted.
     * @return Password of account.
     */
    public String getPassword() {
        return password;
    }

}
