package com.nexusbpm.database.driver;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SQLDriver implements Comparable<SQLDriver> {
    public static final String DRIVERS_PROPERTY_NAME = "JDBC Drivers";
    
    private static final List<SQLDriver> drivers = new ArrayList<SQLDriver>();
    private static final List<String> driverNames = new ArrayList<String>();
    
    private static DriverProperties properties;
    
    private String name;
    private String sampleConnectString;
    private String driverClassName;
    
    private boolean builtIn;
    
    private static void initialize() {
        if(properties == null) {
            properties = new DriverProperties(new SQLDriverCallback());
        }
    }
    
    static final class SQLDriverCallback {
        void addDriver(String name, String sample, String className, boolean builtIn) {
            SQLDriver driver = new SQLDriver(name, sample, className, builtIn);
            drivers.add(driver);
            driverNames.add(name);
        }
        void removeDriver(SQLDriver driver) {
            drivers.remove(driver);
            driverNames.remove(driver.getName());
        }
        void clear(boolean all) {
            if(all) {
                drivers.clear();
                driverNames.clear();
            } else {
                // clear non-built in drivers
                Iterator<SQLDriver> iter = drivers.iterator();
                while(iter.hasNext()) {
                    SQLDriver driver = iter.next();
                    if(!driver.isBuiltIn()) {
                        iter.remove();
                        driverNames.remove(driver.getName());
                    }
                }
            }
        }
        void sortDrivers() {
            Collections.sort(drivers);
            Collections.sort(driverNames);
        }
    }
    
    public static synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        initialize();
        properties.addPropertyChangeListener(listener);
    }
    
    public static synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        initialize();
        properties.removePropertyChangeListener(listener);
    }
    
    public static synchronized List<SQLDriver> getDrivers() {
        initialize();
        return Collections.unmodifiableList(drivers);
    }
    
    public static synchronized List<String> getDriverNames() {
        initialize();
        return Collections.unmodifiableList(driverNames);
    }
    
    public static String getDriversString(List<SQLDriver> drivers) {
        return properties.getDriversString(drivers);
    }
    
    public static synchronized SQLDriver getDriverInstanceByName(String name) {
        initialize();
        for (int index = 0; index < drivers.size(); index++) {
            SQLDriver driver = drivers.get(index);
            if (driver.getName().equals(name)) return driver;
        }
        return null;
    }
    
    public static synchronized SQLDriver getDriverInstanceByClassName(String className) {
        initialize();
        for(int index = 0; index < drivers.size(); index++) {
            SQLDriver driver = drivers.get(index);
            if(driver.getDriverClassName().equals(className)) {
                return driver;
            }
        }
        return null;
    }
    
    public static synchronized SQLDriver getDriverInstanceByClassNameAndURI(String className, String uri) {
        initialize();
        Pattern regex = Pattern.compile("jdbc:\\w*:{0,1}\\w*");
        for (int index = 0; index < drivers.size(); index++) {
            SQLDriver driver = drivers.get(index);
            String sample = driver.getSampleConnectString();
            Matcher m = regex.matcher(sample);
            m.find();
            String split = m.group();
            if (driver.getDriverClassName().equals(className)
                && uri.startsWith(split)
               ) { 
                return driver;
            }
        }
        return null;
    }
    
    public SQLDriver(String name, String sampleConnectString, String driverClassName) {
        if(name == null || sampleConnectString == null || driverClassName == null) {
            throw new IllegalArgumentException("SQL Driver information cannot be null");
        }
        this.name = name;
        this.sampleConnectString = sampleConnectString;
        this.driverClassName = driverClassName;
        this.builtIn = false;
    }
    
    private SQLDriver(String name, String sampleConnectString, String driverClassName, boolean builtIn) {
        this.name = name;
        this.sampleConnectString = sampleConnectString;
        this.driverClassName = driverClassName;
        this.builtIn = builtIn;
    }
    
    public String getDriverClassName() {
        return driverClassName;
    }
    
    public String getName() {
        return name;
    }
    
    public String getSampleConnectString() {
        return sampleConnectString;
    }
    
    public String toString() {
        return getDriverClassName();
    }
    
    public boolean isBuiltIn() {
        return builtIn;
    }
    
    public int compareTo(SQLDriver o) {
        return name.compareTo(o.name);
    }
}
