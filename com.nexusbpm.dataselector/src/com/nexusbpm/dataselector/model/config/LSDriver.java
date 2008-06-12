package com.nexusbpm.dataselector.model.config;

import com.nexusbpm.dataselector.model.NamedModelElement;

public class LSDriver extends NamedModelElement {
    public static final String PROPERTY_DRIVER_CLASS = "driverClass";
    
    private String driverClass = "";
    
    public LSDriver(LSConfig config) {
        super(config);
    }
    
    public String getDriverClass() {
        return driverClass;
    }
    
    public void setDriverClass(String driverClass) {
        String oldDriverClass = this.driverClass;
        this.driverClass = driverClass;
        firePropertyChange(PROPERTY_DRIVER_CLASS, oldDriverClass, driverClass);
    }
}
