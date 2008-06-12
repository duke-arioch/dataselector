package com.nexusbpm.dataselector.requests;

/**
 * Represents a request to get a username and password from the user.
 */
public class LoginRequest extends AbstractUserInputRequest {
    private String username;
    private String password;
    private boolean savePassword;
    
    public LoginRequest() {
    }
    
    public LoginRequest(String username, String password, boolean savePassword) {
        this.username = username;
        this.password = password;
        this.savePassword = savePassword;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public boolean isSavePassword() {
        return savePassword;
    }
    
    public void setSavePassword(boolean savePassword) {
        this.savePassword = savePassword;
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + username + ")";
    }
}
