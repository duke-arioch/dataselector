package com.nexusbpm.dataselector.requests;

import com.nexusbpm.database.driver.SQLDriver;
import com.nexusbpm.multipage.bus.BusRequest;

public class ConfigurationChangeRequest implements BusRequest {
    private SQLDriver sqlDriver;
    private String uri;
    private String username;
    private String password;
    private boolean savePassword;
    private String query;
    
    public ConfigurationChangeRequest(
            SQLDriver sqlDriver,
            String uri,
            String username,
            String password,
            boolean savePassword,
            String query) {
        this.sqlDriver = sqlDriver;
        this.uri = uri;
        this.username = username;
        this.password = password;
        this.savePassword = savePassword;
        this.query = query;
    }
    
    public SQLDriver getSQLDriver() {
        return sqlDriver;
    }
    
    public void setSQLDriver(SQLDriver sqlDriver) {
        this.sqlDriver = sqlDriver;
    }
    
    public String getUri() {
        return uri;
    }
    
    public void setUri(String uri) {
        this.uri = uri;
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
    
    public String getQuery() {
        return query;
    }
    
    public void setQuery(String query) {
        this.query = query;
    }
}
