package com.nexusbpm.database.driver;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class SQLDriver implements Comparable<SQLDriver> {
    public static final String DRIVERS_PROPERTY_NAME = "JDBC Drivers";
    
    private static final List<SQLDriver> drivers = new ArrayList<SQLDriver>();
    private static final List<String> driverNames = new ArrayList<String>();
    
    private static DriverProperties properties;
    
    private String name;
    private String sampleConnectString;
    private String driverClassName;
    
    private String stddevFunction;
    private boolean supportsMinMaxStrings;
    
    private boolean builtIn;
    
    private static void initialize() {
        if(properties == null) {
            properties = new DriverProperties(new SQLDriverCallback());
        }
    }
    
    static final class SQLDriverCallback {
        void addDriver(String name,
                       String sample,
                       String className,
                       String stddevFunction,
                       boolean minMaxStrings,
                       boolean builtIn) {
            SQLDriver driver = new SQLDriver(name, sample, className, stddevFunction, minMaxStrings, builtIn);
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
        SQLDriver ret = null;
        int distance = Integer.MAX_VALUE;
        int matchLength = 0;
        for (int index = 0; index < drivers.size(); index++) {
            SQLDriver driver = drivers.get(index);
            String sample = driver.getSampleConnectString();
            if(driver.getDriverClassName().equals(className)) {
                int newMatchLength = getMatchLength(sample.toLowerCase(), uri.toLowerCase());
                int newDistance = getEditDistance(sample.toLowerCase(), uri.toLowerCase());
                if((matchLength == newMatchLength && newDistance < distance) ||
                        newMatchLength > matchLength) {
                    ret = driver;
                    matchLength = newMatchLength;
                    distance = newDistance;
                }
            }
        }
        return ret;
    }
    
    public SQLDriver(String name,
                     String sampleConnectString,
                     String driverClassName,
                     String stddevFunction,
                     boolean supportsMinMaxStrings) {
        if(name == null || sampleConnectString == null || driverClassName == null) {
            throw new IllegalArgumentException("SQL Driver information cannot be null");
        }
        this.name = name;
        this.sampleConnectString = sampleConnectString;
        this.driverClassName = driverClassName;
        this.stddevFunction = stddevFunction;
        this.supportsMinMaxStrings = supportsMinMaxStrings;
        this.builtIn = false;
    }
    
    private SQLDriver(String name,
                      String sampleConnectString,
                      String driverClassName,
                      String stddevFunction,
                      boolean supportsMinMaxStrings,
                      boolean builtIn) {
        this.name = name;
        this.sampleConnectString = sampleConnectString;
        this.driverClassName = driverClassName;
        this.stddevFunction = stddevFunction;
        this.supportsMinMaxStrings = supportsMinMaxStrings;
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
    
    public String getStdDevFunction() {
        return stddevFunction;
    }
    
    public boolean supportsMinMaxStrings() {
        return supportsMinMaxStrings;
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
    
    protected static int getMatchLength(String string1, String string2) {
        int length = 0;
        for(int index = 0; index < string1.length() && index < string2.length(); index++) {
            if(string1.charAt(index) != string2.charAt(index)) {
                break;
            }
            length += 1;
        }
        return length;
    }
    
    /**
     * Compute Levenshtein (edit) distance.
     */
    protected static int getEditDistance(String string1, String string2) {
        int string1Length = string1.length();
        int string2Length = string2.length();
        
        // Step 1
        if(string1Length == 0) {
            return string2Length;
        }
        if(string2Length == 0) {
            return string1Length;
        }
        int[][] matrix = new int[string1Length + 1][string2Length + 1];
        
        // Step 2
        for(int index1 = 0; index1 <= string1Length; index1++) {
            matrix[index1][0] = index1;
        }
        for(int index2 = 0; index2 <= string2Length; index2++) {
            matrix[0][index2] = index2;
        }
        
        // Step 3
        for(int index1 = 1; index1 <= string1Length; index1++) {
            char c1 = string1.charAt(index1 - 1);
            // Step 4
            for(int index2 = 1; index2 <= string2Length; index2++) {
                char c2 = string2.charAt(index2 - 1);
                // Step 5
                int cost = 1;
                if(c1 == c2) {
                    cost = 0;
                }
                // Step 6
                matrix[index1][index2] =
                    Math.min(Math.min(matrix[index1 - 1][index2] + 1,
                                      matrix[index1][index2 - 1]),
                             matrix[index1 - 1][index2 - 1] + cost);
            }
        }
        
        // Step 7
        return matrix[string1Length][string2Length];
    }
}
