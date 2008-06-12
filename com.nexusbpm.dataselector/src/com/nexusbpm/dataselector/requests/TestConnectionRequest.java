package com.nexusbpm.dataselector.requests;

import com.nexusbpm.database.driver.SQLDriver;
import com.nexusbpm.multipage.bus.BusRequest;

public class TestConnectionRequest implements BusRequest {
    private SQLDriver sqlDriver;
    private String uri;
    private String username;
    private String password;
    
    private boolean useCurrentConfig;
    
    public TestConnectionRequest() {
        useCurrentConfig = true;
    }
    
    public TestConnectionRequest(SQLDriver sqlDriver, String uri, String username, String password) {
        useCurrentConfig = false;
        this.sqlDriver = sqlDriver;
        this.uri = uri;
        this.username = username;
        this.password = password;
    }
    
    public boolean useCurrentConfig() {
        return useCurrentConfig;
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
}
