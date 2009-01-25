package com.nexusbpm.database.prefs;

import com.nexusbpm.database.driver.SQLDriver;

/**
 * Wrapper class used for the preferences page table.
 */
public class SQLDriverWrapper {
    // the original driver, or null if this is a new driver
    protected SQLDriver driver;
    
    // the modified information
    protected String name;
    protected String sampleConnectString;
    protected String driverClassName;
    
    public SQLDriverWrapper(String name, String sampleConnectString, String driverClassName) {
        this.driver = null;
        this.name = name;
        this.sampleConnectString = sampleConnectString;
        this.driverClassName = driverClassName;
    }
    
    public SQLDriverWrapper(SQLDriver driver) {
        this.driver = driver;
        
        if(driver != null) {
            this.name = driver.getName();
            this.sampleConnectString = driver.getSampleConnectString();
            this.driverClassName = driver.getDriverClassName();
        }
    }
    
    public boolean isBuiltIn() {
        return driver != null && driver.isBuiltIn();
    }
    
    public SQLDriver getDriver() {
        return driver;
    }
    
    public void setDriver(SQLDriver driver) {
        this.driver = driver;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getSampleConnectString() {
        return sampleConnectString;
    }
    
    public void setSampleConnectString(String sampleConnectString) {
        this.sampleConnectString = sampleConnectString;
    }
    
    public String getDriverClassName() {
        return driverClassName;
    }
    
    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }
    
    @Override
    public String toString() {
        return name;
    }
}
