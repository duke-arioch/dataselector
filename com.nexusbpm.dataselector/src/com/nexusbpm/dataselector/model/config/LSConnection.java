package com.nexusbpm.dataselector.model.config;

import com.nexusbpm.dataselector.model.AbstractModelElement;

public class LSConnection extends AbstractModelElement {
    public static final String PROPERTY_CONNECTION_URI = "connectionURI";
    public static final String PROPERTY_CONNECTION_USERNAME = "connectionUsername";
    public static final String PROPERTY_CONNECTION_PASSWORD = "connectionPassword";
    public static final String PROPERTY_SAVE_CONNECTION_PASSWORD = "saveConnectionPassword";
    
    private String uri = "";
    private String username = "";
    private String password = "";
    private boolean savePassword;
    
    public LSConnection(LSConfig config) {
        super(config);
    }
    
    public String getURI() {
        return uri;
    }
    
    public void setURI(String uri) {
        String oldURI = this.uri;
        this.uri = uri;
        firePropertyChange(PROPERTY_CONNECTION_URI, oldURI, uri);
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        String oldUsername = this.username;
        this.username = username;
        firePropertyChange(PROPERTY_CONNECTION_USERNAME, oldUsername, username);
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        String oldPassword = this.password;
        this.password = password;
        firePropertyChange(PROPERTY_CONNECTION_PASSWORD, oldPassword, password);
    }
    
    public boolean isSavePassword() {
        return savePassword;
    }
    
    public void setSavePassword(boolean savePassword) {
        Boolean oldSavePassword = Boolean.valueOf(this.savePassword);
        this.savePassword = savePassword;
        firePropertyChange(PROPERTY_SAVE_CONNECTION_PASSWORD, oldSavePassword, Boolean.valueOf(savePassword));
    }
}
